package org.originmc.blockvisualiser.block;

import org.bukkit.Location;
import org.originmc.blockvisualiser.generator.BlockGenerator;

public class FakeBlockImpl implements FakeBlock {

    private final Data data;
    private final Location location;
    private final BlockGenerator generator;

    public FakeBlockImpl(Data data, Location position, BlockGenerator generator) {
        this.data = data;
        this.location = position;
        this.generator = generator;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public BlockGenerator getGenerator() {
        return generator;
    }
}
