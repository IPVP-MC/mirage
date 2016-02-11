package org.originmc.blockvisualiser.block;

import org.originmc.blockvisualiser.Position;
import org.originmc.blockvisualiser.generator.BlockGenerator;

public class FakeBlockImpl implements FakeBlock {

    private final Data data;
    private final Position position;
    private final BlockGenerator generator;

    public FakeBlockImpl(Data data, Position position, BlockGenerator generator) {
        this.data = data;
        this.position = position;
        this.generator = generator;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public BlockGenerator getGenerator() {
        return generator;
    }
}
