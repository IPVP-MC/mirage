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

    @Override
    public int hashCode() {
        int hash = 31;
        hash = 31 * hash + world.hashCode();
        hash = 31 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 31 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 31 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Position)) {
            return false;
        } else {
            Position p = (Position) o;
            return p.world.equals(world)
                    && p.x == x
                    && p.y == y
                    && p.z == z;
        }
    }
}
