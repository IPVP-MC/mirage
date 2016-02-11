package org.originmc.blockvisualiser;

import com.comphenix.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.originmc.blockvisualiser.block.FakeBlock;
import org.originmc.blockvisualiser.block.FakeBlockImpl;
import org.originmc.blockvisualiser.block.SimpleBlockData;
import org.originmc.blockvisualiser.generator.BlockGenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

class FakeBlockSenderImpl implements FakeBlockSender {

    private Map<UUID, Map<Location, FakeBlock>> sentBlocks = new ConcurrentHashMap<>();

    @Override
    public FakeBlock getBlockAt(Player player, Location location) {
        if (!sentBlocks.containsKey(player.getUniqueId())) {
            return null;
        }
        return sentBlocks.get(player.getUniqueId()).get(location);
    }

    @Override
    public void sendBlock(Player player, BlockGenerator generator, Location location) {
        FakeBlock block = createBlock(generator, player, location);
        Map<Location, FakeBlock> sent = sentBlocks.putIfAbsent(player.getUniqueId(), new ConcurrentHashMap<>());
        sent.put(location, block);
        sendSingleBlockChange(player, block);
    }

    @Override
    public void sendBlocks(Player player, BlockGenerator generator, Collection<Location> locations) {
        Map<Location, FakeBlock> send = new HashMap<>();
        locations.forEach(pos -> {
            FakeBlock block = createBlock(generator, player, pos);
            send.put(pos, block);
        });
        addSentBlocks(player, send);
        handleBlockChanges(player, send.values());
    }

    // Creates a fake block
    private FakeBlock createBlock(BlockGenerator generator, Player player, Location location) {
        FakeBlock.Data data = generator.getData(player, location);
        return new FakeBlockImpl(data, location, generator);
    }

    // Updates sentBlocks with a map of newly sent blocks
    private void addSentBlocks(Player player, Map<Location, FakeBlock> blocks) {
        Map<Location, FakeBlock> sent = sentBlocks.putIfAbsent(player.getUniqueId(), new ConcurrentHashMap<>());
        sent.putAll(blocks);
    }

    @Override
    public void clearBlockAt(Player player, Location location) {
        if (!sentBlocks.containsKey(player.getUniqueId())) {
            return;
        }
        Map<Location, FakeBlock> sent = sentBlocks.get(player.getUniqueId());
        FakeBlock block = sent.remove(location);
        if (block != null) {
            Block current = location.getBlock();
            player.sendBlockChange(location, current.getType(), current.getData());
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
        Map<Location, FakeBlock> sent = sentBlocks.remove(player.getUniqueId());
        Set<FakeBlock> update = new HashSet<>();
        sent.values().stream()
                .filter(test::test) // Filter unwanted
                .forEach(fakeBlock -> {
                    FakeBlock.Data data = getCurrentData(fakeBlock);
                    FakeBlock current = new FakeBlockImpl(data, fakeBlock.getLocation(), fakeBlock.getGenerator());
                    update.add(current);
                });
        handleBlockChanges(player, update);
    }

    // Gets an updated block data for a fake block
    private FakeBlock.Data getCurrentData(FakeBlock block) {
        Block current = block.getLocation().getBlock();
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
            Location location = block.getLocation();
            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;
            if (location.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                pairs.put(location.getChunk(), block);
            }
        });

        //  TODO: possible ConcurrentModification with pairs.get(chunk)
        pairs.keySet().forEach(chunk -> sendBulkBlockChange(player, chunk, pairs.get(chunk)));
    }

    // Sends a single block change to a player
    private void sendSingleBlockChange(Player player, FakeBlock block) {
        player.sendBlockChange(block.getLocation(), block.getData().getType(), block.getData().getData());
    }

    // Sends a bulk block change in a chunk to a player
    private void sendBulkBlockChange(Player player, Chunk chunk, Set<FakeBlock> blocks) {
        MultiBlockChangeInfo[] blockChangeInfo = new MultiBlockChangeInfo[blocks.size()];
        int i = 0;
        for (FakeBlock block : blocks) {
            FakeBlock.Data data = block.getData();
            blockChangeInfo[i++] = new MultiBlockChangeInfo(block.getLocation(), WrappedBlockData.createData(data.getType()));
        }
        WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange();
        packet.setChunk(new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
        packet.setRecords(blockChangeInfo);
        packet.sendPacket(player);
    }
}
