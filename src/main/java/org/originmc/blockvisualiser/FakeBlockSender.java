package org.originmc.blockvisualiser;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.originmc.blockvisualiser.block.FakeBlock;
import org.originmc.blockvisualiser.generator.BlockGenerator;

import java.util.Collection;
import java.util.function.Predicate;

public interface FakeBlockSender {

    /**
     * Gets a sent {@link FakeBlock} for a player at a specific location.
     *
     * @param player the player
     * @param location the location
     * @return the sent block
     */
    FakeBlock getBlockAt(Player player, Vector location);

    /**
     * Sends a single {@link FakeBlock} to a player at a specific location. This method
     * uses the provided {@link BlockGenerator} implementation to create fake block
     * data which is used to create a fake block to send.
     *
     * @param player the player
     * @param generator the generator
     * @param location the location
     */
    void sendBlock(Player player, BlockGenerator generator, Vector location);

    /**
     * Sends multiple {@link FakeBlock}s to a player at given locations.
     *
     * @param player the player
     * @param generator the generator
     * @param locations the location
     * @return the number of blocks sent
     */
    int sendBlocks(Player player, BlockGenerator generator, Collection<Vector> locations);

    /**
     * Reverts a fake block that was sent to a player.
     *
     * @param player the player
     * @param location the location of the block
     */
    void clearBlockAt(Player player, Vector location);

    /**
     * Reverts all blocks that have been sent to a player.
     *
     * @param player the player
     */
    void clearBlocks(Player player);

    /**
     * Reverts all blocks that have been sent to a player and match a condition.
     *
     * @param player the player
     * @param test the condition
     */
    void clearBlocks(Player player, Predicate<FakeBlock> test);

}