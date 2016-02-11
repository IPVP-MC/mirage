package org.originmc.blockvisualiser;

import com.google.common.collect.Iterables;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Represents how visual blocks are filled.
 */
interface LocationData {

    VisualBlockData getData(Player player, Location location);

    default ArrayList<VisualBlockData> bulkGenerate(Player player, Iterable<Location> locations) {
        ArrayList<VisualBlockData> data = new ArrayList<>(Iterables.size(locations));
        for (Location location : locations) {
            data.add(getData(player, location));
        }

        return data;
    }
}
