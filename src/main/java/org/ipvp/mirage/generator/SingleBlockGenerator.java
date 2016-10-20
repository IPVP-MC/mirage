package org.ipvp.mirage.generator;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.ipvp.mirage.block.FakeBlock.Data;
import org.ipvp.mirage.block.SimpleBlockData;

public class SingleBlockGenerator implements BlockGenerator {

    private final Data data;

    public SingleBlockGenerator(Material type, byte data) {
        this(new SimpleBlockData(type, data));
    }

    public SingleBlockGenerator(Data data) {
        this.data = data;
    }

    /**
     * Returns the type of block that is being generated.
     *
     * @return the type
     */
    public Material getType() {
        return data.getType();
    }

    @Override
    public Data getData(Player player, Vector location) {
        return data;
    }
}
