package org.originmc.blockvisualiser;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.originmc.blockvisualiser.block.FakeBlock;
import org.originmc.blockvisualiser.block.FakeBlockImpl;
import org.originmc.blockvisualiser.block.SimpleBlockData;
import org.originmc.blockvisualiser.generator.BlockGenerator;
import org.spigotmc.SpigotDebreakifier;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

class FakeBlockSenderImpl implements FakeBlockSender {

    private Map<UUID, Map<Vector, FakeBlock>> sentBlocks = new ConcurrentHashMap<>();

    @Override
    public FakeBlock getBlockAt(Player player, Vector location) {
        if (!sentBlocks.containsKey(player.getUniqueId())) {
            return null;
        }
        return sentBlocks.get(player.getUniqueId()).get(location);
    }

    @Override
    public void sendBlock(Player player, BlockGenerator generator, Vector location) {
        FakeBlock block = createBlock(generator, player, location);
        Map<Vector, FakeBlock> sent = getSentBlocks(player);
        sent.put(location, block);
        sendSingleBlockChange(player, block);
    }

    @Override
    public int sendBlocks(Player player, BlockGenerator generator, Collection<Vector> locations) {
        Map<Vector, FakeBlock> send = new HashMap<>();
        locations.forEach(pos -> {
            FakeBlock block = createBlock(generator, player, pos);
            send.put(pos, block);
        });
        addSentBlocks(player, send);
        handleBlockChanges(player, send.values());
        return send.size();
    }

    // Creates a fake block
    private FakeBlock createBlock(BlockGenerator generator, Player player, Vector location) {
        FakeBlock.Data data = generator.getData(player, location);
        return new FakeBlockImpl(data, player.getWorld(), location, generator);
    }

    // Updates sentBlocks with a map of newly sent blocks
    private void addSentBlocks(Player player, Map<Vector, FakeBlock> blocks) {
        Map<Vector, FakeBlock> sent = getSentBlocks(player);
        sent.putAll(blocks);
    }

    private Map<Vector, FakeBlock> getSentBlocks(Player player) {
        Map<Vector, FakeBlock> sent;
        if (!sentBlocks.containsKey(player.getUniqueId())) {
            sent = new ConcurrentHashMap<>();
            sentBlocks.put(player.getUniqueId(), sent);
        } else {
            sent = sentBlocks.get(player.getUniqueId());
        }
        return sent;
    }

    @Override
    public void clearBlockAt(Player player, Vector location) {
        if (!sentBlocks.containsKey(player.getUniqueId())) {
            return;
        }
        Map<Vector, FakeBlock> sent = sentBlocks.get(player.getUniqueId());
        FakeBlock block = sent.remove(location);
        if (block != null) {
            Location loc = new Location(player.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
            Block current = loc.getBlock();
            player.sendBlockChange(loc, current.getType(), current.getData());
        }
    }

    @Override
    public void clearBlocks(Player player) {
        clearBlocks(player, fakeBlock -> true);
    }

    @Override
    public void clearBlocks(Player player, Predicate<FakeBlock> test) {
        if (!sentBlocks.containsKey(player.getUniqueId())) {
            return;
        }
        Map<Vector, FakeBlock> sent = sentBlocks.get(player.getUniqueId());
        Set<FakeBlock> update = new HashSet<>();
        sent.values().stream()
                .filter(test::test) // Filter unwanted
                .forEach(fakeBlock -> {
                    Vector fakeLocation = fakeBlock.getLocation();
                    if (fakeBlock.getWorld().isChunkLoaded(fakeLocation.getBlockX() >> 4, fakeLocation.getBlockZ() >> 4)) {
                        FakeBlock.Data data = getCurrentData(fakeBlock);
                        FakeBlock current = new FakeBlockImpl(data, fakeBlock.getWorld(), fakeLocation, fakeBlock.getGenerator());
                        update.add(current);
                    }
                    sent.remove(fakeLocation);
                });
        handleBlockChanges(player, update);
        if (sent.isEmpty()) {
            sentBlocks.remove(player.getUniqueId());
        }
    }

    // Gets an updated block data for a fake block
    private FakeBlock.Data getCurrentData(FakeBlock block) {
        Vector loc = block.getLocation();
        Block current = block.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return new SimpleBlockData(current.getType(), current.getData());
    }

    // Handle block changes for a player
    private void handleBlockChanges(Player player, Collection<FakeBlock> input) {
        if (input.isEmpty()) {
            return;
        }

        if (input.size() == 1) {
            sendSingleBlockChange(player, input.iterator().next());
            return;
        }

        SetMultimap<Chunk, FakeBlock> pairs = HashMultimap.create();
        input.forEach(block -> {
            Vector location = block.getLocation();
            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;
            if (block.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                Chunk chunk = block.getWorld().getChunkAt(chunkX, chunkZ);
                pairs.put(chunk, block);
            }
        });

        try {
            for (Chunk chunk : pairs.keySet()) {
                // TODO: possible ConcurrentModification with pairs.get(chunk)
                sendBulkBlockChange(player, chunk, pairs.get(chunk));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Sends a single block change to a player
    private void sendSingleBlockChange(Player player, FakeBlock block) {
        Vector location = block.getLocation();
        Location loc = new Location(player.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        player.sendBlockChange(loc, block.getData().getType(), block.getData().getData());
    }

    // Sends a bulk block change in a chunk to a player
    private void sendBulkBlockChange(Player player, Chunk chunk, Set<FakeBlock> blocksToSend) throws IOException {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(blocksToSend.size());
        DataOutputStream dataOutputStream = new DataOutputStream(byteOutputStream);

        short[] ashort = new short[blocksToSend.size()];
        int[] blocks = new int[blocksToSend.size()];

        int i = 0;
        for (FakeBlock block : blocksToSend) {
            Vector location = block.getLocation();
            int blockID = block.getData().getType().getId();
            int data = block.getData().getData();
            data = SpigotDebreakifier.getCorrectedData(blockID, data);

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
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
