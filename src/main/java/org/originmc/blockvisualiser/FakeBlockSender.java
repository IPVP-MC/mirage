package org.originmc.blockvisualiser;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.originmc.blockvisualiser.block.FakeBlock;
import org.originmc.blockvisualiser.generator.BlockGenerator;

import java.util.Collection;
import java.util.function.Predicate;

public interface FakeBlockSender {

    FakeBlock getBlockAt(Player player, Location location);

    void sendBlock(Player player, BlockGenerator generator, Location location);

    void sendBlocks(Player player, BlockGenerator generator, Collection<Location> locations);

    void clearBlockAt(Player player, Location location);

    void clearBlocks(Player player);

    void clearBlocks(Player player, Predicate<? extends FakeBlock> test);

}