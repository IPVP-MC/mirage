package org.originmc.blockvisualiser;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.originmc.blockvisualiser.block.FakeBlock;
import org.originmc.blockvisualiser.block.FakeBlockImpl;
import org.originmc.blockvisualiser.generator.BlockGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class VisualiseHandler {

    private final Table<UUID, Location, FakeBlock> storedVisualises = HashBasedTable.create();

    /**
     * Gets a {@link FakeBlock} for a {@link Player}.
     *
     * @param player the {@link Player} to get for
     * @param location the {@link Location} to get at
     * @return the {@link FakeBlock} or none
     * @throws NullPointerException if player or location is null
     */
    public FakeBlock getFakeBlockAt(Player player, Location location) throws NullPointerException {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(location, "Location cannot be null");
        return storedVisualises.get(player.getUniqueId(), location);
    }

    /**
     * Gets the current {@link FakeBlock}s to their {@link Location}s that are shown
     * to a {@link Player} of a specific {@link VisualType}.
     *
     * @param player the {@link Player} to get for
     * @return copied map of {@link FakeBlock}s shown to a {@link Player}.
     */
    public Map<Location, FakeBlock> getFakeBlocks(Player player) {
        return new HashMap<>(storedVisualises.row(player.getUniqueId()));
    }

    /**
     * Gets the current {@link FakeBlock}s to their {@link Location}s that are shown
     * to a {@link Player} of a specific {@link VisualType}.
     *
     * @param player the {@link Player} to get for
     * @param generator the {@link VisualType} to get for
     * @return copied map of {@link FakeBlock}s shown to a {@link Player}.
     */
    public Map<Location, FakeBlock> getFakeBlocks(Player player, BlockGenerator generator) {
        return Maps.filterValues(getFakeBlocks(player), block -> block.getGenerator().equals(generator));
    }

    private List<FakeBlock.Data> bulkGenerate(BlockGenerator type, Player player, Iterable<Location> locations) {
        List<FakeBlock.Data> data = new ArrayList<>();
        locations.forEach(location -> data.add(type.getData(player, location)));
        return data;
    }

    public LinkedHashMap<Location, FakeBlock.Data> generate(Player player, Iterable<Location> locations, BlockGenerator generator, boolean canOverwrite) {
        LinkedHashMap<Location, FakeBlock.Data> results = new LinkedHashMap<>();

        List<FakeBlock.Data> filled = bulkGenerate(generator, player, locations);
        if (filled != null) {
            int count = 0;
            Map<Location, FakeBlock.Data> updatedBlocks = new HashMap<>();
            for (Location location : locations) {
                if (!canOverwrite && storedVisualises.contains(player.getUniqueId(), location)) {
                    continue;
                }

                Material previousType = location.getBlock().getType();
                if (previousType.isSolid() || previousType != Material.AIR) {
                    continue;
                }

                FakeBlock.Data data = filled.get(count++);
                results.put(location, data);
                updatedBlocks.put(location, data);
                storedVisualises.put(player.getUniqueId(), location, new FakeBlockImpl(data, new Position(location), generator));
            }

            try {
                VisualiseUtil.handleBlockChanges(player, updatedBlocks);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return results;
    }

    /**
     * Clears a visual block at a given location for a player.
     *
     * @param player the player to clear for
     * @param location the location to clear at
     * @return if the visual block was shown in the first place
     */
    public void clearFakeBlock(Player player, Location location) {
        clearFakeBlock(player, location, true);
    }

    /**
     * Clears a visual block at a given location for a player.
     *
     * @param player the player to clear for
     * @param location the location to clear at
     * @param sendRemovalPacket if a packet to send a block change should be sent
     * (this is used to prevent unnecessary packets sent when
     * disconnecting or changing worlds, for example)
     * @return if the visual block was shown in the first place
     */
    public void clearFakeBlock(Player player, Location location, boolean sendRemovalPacket) {
        FakeBlock FakeBlock = storedVisualises.remove(player.getUniqueId(), location);
        if (sendRemovalPacket && FakeBlock != null) {
            // Have to send a packet to the original block type, don't send if the fake block has the same data properties though.
            Block block = location.getBlock();
            FakeBlock.Data data = FakeBlock.getData();
            if (data.getType() != block.getType() || data.getData() != block.getData()) {
                player.sendBlockChange(location, block.getType(), block.getData());
            }
        }
    }

    /**
     * Clears all visual blocks in a {@link Chunk}.
     *
     * @param chunk the {@link Chunk} to clear in
     */
    public void clearFakeBlocks(Chunk chunk) {
        if (!storedVisualises.isEmpty()) {
            Set<Location> keys = storedVisualises.columnKeySet();
            new HashSet<>(keys).stream()
                    .filter(location -> location.getWorld().equals(chunk.getWorld())
                            && chunk.getX() == (((int) location.getX()) >> 4)
                            && chunk.getZ() == (((int) location.getZ()) >> 4))
                    .forEach(keys::remove);
        }
    }

    /**
     * Clears all visual blocks that are shown to a player.
     *
     * @param player the player to clear for
     */
    public void clearFakeBlocks(Player player) {
        clearFakeBlocks(player, null, null);
    }

    /**
     * Clears all visual blocks that are shown to a player of a given VisualType.
     *
     * @param player the player to clear for
     * @param generator the visual type
     * @param predicate the predicate to filter to
     */
    public void clearFakeBlocks(Player player, BlockGenerator generator, Predicate<FakeBlock> predicate) {
        clearFakeBlocks(player, generator, predicate, true);
    }

    /**
     * Clears all visual blocks that are shown to a player of a given VisualType.
     *
     * @param player the player to clear for
     * @param generator the visual type
     * @param predicate the predicate to filter to
     * @param sendRemovalPackets if a packet to send a block change should be sent
     * (this is used to prevent unnecessary packets sent when
     * disconnecting or changing worlds, for example)
     */
    @Deprecated
    public void clearFakeBlocks(Player player,
                                  BlockGenerator generator,
                                  Predicate<FakeBlock> predicate,
                                  boolean sendRemovalPackets) {
        if (!storedVisualises.containsRow(player.getUniqueId())) {
            return;
        }

        Map<Location, FakeBlock> results = new HashMap<>(storedVisualises.row(player.getUniqueId())); // copy to prevent commodification
        Map<Location, FakeBlock> removed = new HashMap<>();
        for (Map.Entry<Location, FakeBlock> entry : results.entrySet()) {
            FakeBlock block = entry.getValue();
            if ((predicate == null || predicate.test(block)) && (generator == null || generator.equals(block.getGenerator()))) {
                Location location = entry.getKey();
                if (removed.put(location, block) == null) { // not really necessary, but might as well
                    clearFakeBlock(player, location, sendRemovalPackets); // this will call remove on storedVisualises.
                }
            }
        }
    }
}
