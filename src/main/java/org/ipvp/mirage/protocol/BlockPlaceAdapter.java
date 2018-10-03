package org.ipvp.mirage.protocol;

import java.util.Optional;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.mirage.FakeBlockSender;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import org.ipvp.mirage.block.FakeBlock;

public class BlockPlaceAdapter extends PacketAdapter {

    public BlockPlaceAdapter(JavaPlugin plugin) {
        super(plugin, ListenerPriority.LOWEST, PacketType.Play.Client.BLOCK_PLACE);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        StructureModifier<EnumWrappers.Direction> modifier = event.getPacket().getDirections();
        StructureModifier<BlockPosition> positions = event.getPacket().getBlockPositionModifier();

        Player player = event.getPlayer();

        try {
            EnumWrappers.Direction face = modifier.read(0);
            event.getPlayer().sendMessage(face.name());

            BlockPosition position = positions.read(0);
            int x = position.getX(), y = position.getY(), z = position.getZ();
            Location clickedBlock = new Location(player.getWorld(), x, y, z);
            clickedBlock = clickedBlock.getBlock().getLocation();

            Optional<FakeBlockSender> opt = FakeBlockSender.getFrom(player);
            if (!opt.isPresent()) {
                return;
            }

            FakeBlockSender sender = opt.get();
            FakeBlock block = sender.getBlockAt(clickedBlock.toVector());

            event.getPlayer().sendMessage("clicked block: " + block);

            if (block == null) {
                return;
            }

            Location placedLocation = clickedBlock.clone();
            switch (face) {
                case UP:
                    placedLocation.add(0, -1, 0);
                    break;
                case DOWN:
                    placedLocation.add(0, 1, 0);
                    break;
                case NORTH:
                    placedLocation.add(0, 0, 1);
                    break;
                case SOUTH:
                    placedLocation.add(0, 0, -1);
                    break;
                case EAST:
                    placedLocation.add(1, 0, 0);
                    break;
                case WEST:
                    placedLocation.add(-1, 0, 0);
                    break;
                default:
                    return;
            }

            event.getPlayer().sendMessage("cancelled");
            event.setCancelled(true);

            // Revert the block placing for the player
            if (sender.getBlockAt(placedLocation.toVector()) == null) {
                player.sendBlockChange(placedLocation, Material.AIR.createBlockData());
                player.updateInventory();
            }
        } catch (FieldAccessException ex) {
            ex.printStackTrace();
        }
    }
}
