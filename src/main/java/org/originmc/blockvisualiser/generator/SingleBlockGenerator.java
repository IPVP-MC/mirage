package org.originmc.blockvisualiser.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.originmc.blockvisualiser.block.FakeBlock.Data;
import org.originmc.blockvisualiser.block.SimpleBlockData;

public class SingleBlockGenerator implements BlockGenerator {

    private final Data data;

    public SingleBlockGenerator(Material type, byte data) {
        this(new SimpleBlockData(type, data));
    }

    public SingleBlockGenerator(Data data) {
        this.data = data;
    }

    @Override
    public Data getData(Player player, Location location) {
        return data;
    }
}
