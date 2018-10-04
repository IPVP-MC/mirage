package org.ipvp.mirage.block;

import org.bukkit.World;
import org.bukkit.util.Vector;
import org.ipvp.mirage.generator.BlockGenerator;

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
    public Vector getVector() {
        return position;
    }

    @Override
    public BlockGenerator getGenerator() {
        return generator;
    }

    @Override
    public boolean usingGenerator(BlockGenerator generator) {
        return getGenerator().equals(generator);
    }
}
