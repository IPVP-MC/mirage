package org.ipvp.mirage.block;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class SimpleBlockData implements FakeBlock.Data {

    private final BlockData data;

    public SimpleBlockData(Material type) {
        this(type.createBlockData());
    }

    public SimpleBlockData(BlockData data) {
        Preconditions.checkNotNull(data, "Data cannot be null");
        this.data = data;
    }

    @Override
    public Material getType() {
        return data.getMaterial();
    }

    @Override
    public BlockData getData() {
        return data;
    }
}
