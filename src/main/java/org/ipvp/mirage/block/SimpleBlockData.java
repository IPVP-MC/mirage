package org.ipvp.mirage.block;

import com.google.common.base.Preconditions;
import org.bukkit.Material;

public class SimpleBlockData implements FakeBlock.Data {

    private final Material type;
    private final byte data;

    public SimpleBlockData(Material type) {
        this(type, (byte) 0);
    }

    public SimpleBlockData(Material type, byte data) {
        Preconditions.checkNotNull(type, "Type cannot be null");
        this.type = type;
        this.data = data;
    }

    @Override
    public Material getType() {
        return type;
    }

    @Override
    public byte getData() {
        return data;
    }
}
