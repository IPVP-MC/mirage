package org.originmc.blockvisualiser;

import org.bukkit.Location;

public class Position {

    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public Position(Location location) {
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Position(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public final String getWorld() {
        return world;
    }

    public final int getX() {
        return x;
    }

    public final int getY() {
        return y;
    }

    public final int getZ() {
        return z;
    }
}
