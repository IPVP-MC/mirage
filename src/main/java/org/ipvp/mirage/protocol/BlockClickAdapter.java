package org.ipvp.mirage.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.mirage.FakeBlockSender;
import org.ipvp.mirage.block.FakeBlock;

import java.util.Optional;

public class BlockClickAdapter extends PacketAdapter {

    private final BlockFace[] FACES_TO_CHECK = new BlockFace[] {BlockFace.SELF, BlockFace.UP, BlockFace.DOWN, BlockFace.EAST,
            BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};

    public BlockClickAdapter(JavaPlugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ITEM);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        StructureModifier<BlockPosition> positions = event.getPacket().getBlockPositionModifier();
        StructureModifier<EnumWrappers.Direction> directions = event.getPacket().getDirections();


        Player player = event.getPlayer();
        BlockPosition position = positions.read(0);
        int x = position.getX(), y = position.getY(), z = position.getZ();
        Location location = new Location(player.getWorld(), x, y, z);

        Optional<FakeBlockSender> sender = FakeBlockSender.getFrom(player);
        if (!sender.isPresent()) {
            return;
        }

        for (BlockFace face : FACES_TO_CHECK) {
            Block block = location.getBlock().getRelative(face);
            FakeBlock visualBlock = sender.get().getBlockAt(block.getLocation().toVector());
            if (visualBlock != null) {
                event.setCancelled(true);
                player.sendBlockChange(location, visualBlock.getData().getData());
                player.updateInventory();
            }
        }

        if (event.isCancelled()) {
            EnumWrappers.Direction direction = directions.read(0);
            BlockFace bukkit = BlockFace.valueOf(direction.name());
            Block block = location.getBlock().getRelative(bukkit);

            player.sendBlockChange(block.getLocation(), Material.AIR.createBlockData());
        }
    }
}
