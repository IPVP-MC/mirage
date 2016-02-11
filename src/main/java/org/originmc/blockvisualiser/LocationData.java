package org.originmc.blockvisualiser;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Represents how visual blocks are filled.
 */
interface LocationData {

    /**
     * Gets block data to show a player for a specific location
     *
     * @param player the player
     * @param location the location
     * @return the data
     */
    VisualBlockData getData(Player player, Location location);
}
