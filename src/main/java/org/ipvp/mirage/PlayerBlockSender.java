package org.ipvp.mirage;

import com.comphenix.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.ipvp.mirage.block.FakeBlock;
import org.ipvp.mirage.block.FakeBlockImpl;
import org.ipvp.mirage.block.SimpleBlockData;
import org.ipvp.mirage.generator.BlockGenerator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class PlayerBlockSender implements FakeBlockSender {

    private Player who;
    private Map<Vector, FakeBlock> sentBlocks = new ConcurrentHashMap<>();

    /**
     * Initializes a fake block sender for a specific player
     *
     * @param player Player to recieve fake blocks
     */
    public PlayerBlockSender(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        this.who = player;
    }

    @Override
    public Player getPlayer() {
        return who;
    }

    @Override
    public FakeBlock getBlockAt(Vector location) {
        return sentBlocks.get(location);
    }

    @Override
    public void sendBlock(BlockGenerator generator, Vector location) {
        FakeBlock block = createBlock(generator, location);
        sentBlocks.put(new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ()), block);
        sendSingleBlockChange(block);
    }

    @Override
    public int sendBlocks(BlockGenerator generator, Collection<Vector> locations) {
        Map<Vector, FakeBlock> send = new HashMap<>();
        locations.forEach(pos -> {
            FakeBlock block = createBlock(generator, pos);
            send.put(new Vector(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()), block);
        });
        sentBlocks.putAll(send);
        handleBlockChanges(send.values());
        return send.size();
    }

    // Creates a fake block
    private FakeBlock createBlock(BlockGenerator generator, Vector location) {
        FakeBlock.Data data = generator.getData(who, location);
        return new FakeBlockImpl(data, who.getWorld(), location, generator);
    }

    @Override
    public void clearBlockAt(Vector location) {
        FakeBlock block = sentBlocks.remove(location);
        if (block != null) {
            Location loc = new Location(who.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
            Block current = loc.getBlock();
            who.sendBlockChange(loc, current.getBlockData());
        }
    }

    @Override
    public void clearBlocks() {
        clearBlocks(fakeBlock -> true);
    }

    @Override
    public void clearBlocks(Predicate<FakeBlock> test) {
        Set<FakeBlock> update = new HashSet<>();
        sentBlocks.values().stream()
                .filter(test) // Filter unwanted
                .forEach(fakeBlock -> {
                    Vector fakeLocation = fakeBlock.getVector();
                    if (fakeBlock.getWorld().isChunkLoaded(fakeLocation.getBlockX() >> 4, fakeLocation.getBlockZ() >> 4)) {
                        FakeBlock.Data data = getCurrentData(fakeBlock);
                        FakeBlock current = new FakeBlockImpl(data, fakeBlock.getWorld(), fakeLocation, fakeBlock.getGenerator());
                        update.add(current);
                    }
                    sentBlocks.remove(fakeLocation);
                });
        handleBlockChanges(update);
    }

    // Gets an updated block data for a fake block
    private FakeBlock.Data getCurrentData(FakeBlock block) {
        Vector loc = block.getVector();
        Block current = block.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return new SimpleBlockData(current.getBlockData());
    }

    // Handle block changes for a player
    private void handleBlockChanges(Collection<FakeBlock> input) {
        if (input.isEmpty()) {
            return;
        }

        if (input.size() == 1) {
            sendSingleBlockChange(input.iterator().next());
            return;
        }

        SetMultimap<Chunk, FakeBlock> pairs = HashMultimap.create();
        input.forEach(block -> {
            Vector location = block.getVector();
            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;
            if (block.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                Chunk chunk = block.getWorld().getChunkAt(chunkX, chunkZ);
                pairs.put(chunk, block);
            }
        });

        try {
            for (Chunk chunk : pairs.keySet()) {
                sendBulkBlockChange(chunk, pairs.get(chunk));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Sends a single block change to a player
    private void sendSingleBlockChange(FakeBlock block) {
        Location loc = block.getLocation();
        who.sendBlockChange(loc, block.getData().getData());
    }

    // Sends a bulk block change in a chunk to a player
    private void sendBulkBlockChange(Chunk chunk, Set<FakeBlock> blocksToSend) throws IOException {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
        WrapperPlayServerMultiBlockChange wrapper = new WrapperPlayServerMultiBlockChange(packet);

        MultiBlockChangeInfo[] records = new MultiBlockChangeInfo[blocksToSend.size()];

        int i = 0;
        for (FakeBlock block : blocksToSend) {
            records[i++] = new MultiBlockChangeInfo(block.getLocation(), WrappedBlockData.createData(block.getData().getData()));
        }

        wrapper.setChunk(new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
        wrapper.setRecords(records);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(getPlayer(), wrapper.getHandle());
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        /*
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(blocksToSend.size());
        DataOutputStream dataOutputStream = new DataOutputStream(byteOutputStream);

        short[] ashort = new short[blocksToSend.size()];
        int[] blocks = new int[blocksToSend.size()];

        int i = 0;
        for (FakeBlock block : blocksToSend) {
            Vector location = block.getLocation();
            int blockID = block.getData().getType().getId();
            int data = block.getData().getData();
            // data = SpigotDebreakifier.getCorrectedData(blockID, data);

            blocks[i] = ((blockID & 0xFFF) << 4 | data & 0xF);
            ashort[i] = ((short) ((location.getBlockX() & 0xF) << 12 | (location.getBlockZ() & 0xF) << 8 | location.getBlockY()));

            dataOutputStream.writeShort(ashort[i]);
            dataOutputStream.writeShort(blocks[i]);
            i++;
        }

        int expectedSize = blocksToSend.size() * 4;
        byte[] bulk = byteOutputStream.toByteArray();
        if (bulk.length != expectedSize) {
            throw new IOException("Expected length: '" + expectedSize + "' doesn't match the generated length: '" + bulk.length + "'");
        }

        // Write the data to the packet
        packet.getChunkCoordIntPairs().write(0, new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
        packet.getByteArrays().write(0, bulk);
        packet.getIntegers().write(0, blocksToSend.size());
        packet.getSpecificModifier(short[].class).write(0, ashort);
        packet.getIntegerArrays().write(0, blocks);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(getPlayer(), packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }*/
    }
}
