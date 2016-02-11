package org.originmc.blockvisualiser.generator;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.originmc.blockvisualiser.VisualBlockData;

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
    VisualBlockData getData(Player player, Location location);
}
