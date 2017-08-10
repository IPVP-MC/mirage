package org.ipvp.mirage;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.ipvp.mirage.block.FakeBlock;
import org.ipvp.mirage.generator.BlockGenerator;

/**
 * Represents a fake block sender for a player
 */
public interface FakeBlockSender {

    /**
     * Metadata where the block sender of a player is stored
     */
    String BLOCK_SENDER_META = "mirage/player-block-sender";

    /**
     * Returns a stored block sender from a player
     * 
     * @param player A player that might store the block sender
     * @return Players block sender instance
     */
    static Optional<FakeBlockSender> getFrom(Player player) {
        if (!player.hasMetadata(BLOCK_SENDER_META)) {
            return Optional.empty();
        }
        Object sender = player.getMetadata(BLOCK_SENDER_META).get(0).value();
        return sender instanceof FakeBlockSender ? Optional.of((FakeBlockSender) sender) : Optional.empty();
    }
    
    /**
     * Returns the player that this block sender is sending blocks to
     * 
     * @return Player to send to
     */
    Player getPlayer();
    
    /**
     * Gets a sent {@link FakeBlock} for the player at a specific location.
     *
     * @param location the location
     * @return the sent block
     */
    FakeBlock getBlockAt(Vector location);

    /**
     * Sends a single {@link FakeBlock} to the player at a specific location. This method
     * uses the provided {@link BlockGenerator} implementation to create fake block
     * data which is used to create a fake block to send.
     *
     * @param generator the generator
     * @param location the location
     */
    void sendBlock(BlockGenerator generator, Vector location);

    /**
     * Sends multiple {@link FakeBlock}s to the player at given locations.
     *
     * @param generator the generator
     * @param locations the location
     * @return the number of blocks sent
     */
    int sendBlocks(BlockGenerator generator, Collection<Vector> locations);

    /**
     * Reverts a fake block that was sent to the player.
     *
     * @param location the location of the block
     */
    void clearBlockAt(Vector location);

    /**
     * Reverts all blocks that have been sent to the player.
     */
    void clearBlocks();

    /**
     * Reverts all blocks that have been sent to the player and match a condition.
     *
     * @param test the condition
     */
    void clearBlocks(Predicate<FakeBlock> test);

}