package org.originmc.blockvisualiser;

import org.bukkit.Location;

public class VisualBlock {

    private final VisualType visualType;
    private final VisualBlockData blockData;
    private final Location location;

    public VisualBlock(VisualType type, VisualBlockData data, Location location) {
        this.visualType = type;
        this.blockData = data;
        this.location = location;
    }

    public VisualType getVisualType() {
        return visualType;
    }

    public VisualBlockData getBlockData() {
        return blockData;
    }

    public Location getLocation() {
        return location;
    }
}
