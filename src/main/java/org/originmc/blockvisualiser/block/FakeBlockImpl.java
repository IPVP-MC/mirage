package org.originmc.blockvisualiser.block;

import org.bukkit.World;
import org.bukkit.util.Vector;
import org.originmc.blockvisualiser.generator.BlockGenerator;

public class FakeBlockImpl implements FakeBlock {

    private final Data data;
    private final World world;
    private final Vector position;
    private final BlockGenerator generator;

    public FakeBlockImpl(Data data, World world, Vector position, BlockGenerator generator) {
        this.data = data;
        this.world = world;
        this.position = position;
        this.generator = generator;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Vector getLocation() {
        return position;
    }

    @Override
    public BlockGenerator getGenerator() {
        return generator;
    }
}
