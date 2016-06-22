package org.originmc.blockvisualiser.generator;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.originmc.blockvisualiser.block.FakeBlock.Data;

/**
 * Represents how visual blocks are filled.
 */
public interface BlockGenerator {

    /**
     * Gets block data to show a player for a specific location
     *
     * @param player the player
     * @param location the location
     * @return the data
     */
    Data getData(Player player, Vector location);
}
