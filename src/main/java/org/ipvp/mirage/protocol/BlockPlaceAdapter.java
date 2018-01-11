package org.ipvp.mirage.protocol;

import java.util.Optional;

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
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_PLACE);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        StructureModifier<Integer> modifier = event.getPacket().getIntegers();
        Player player = event.getPlayer();

        try {
            int face = modifier.read(3);
            if (face == 255) {
                return;
            }

            int x = modifier.read(0);
            int y = modifier.read(1);
            int z = modifier.read(2);
            Location clickedBlock = new Location(player.getWorld(), x, y, z);
            clickedBlock = clickedBlock.getBlock().getLocation();

            Optional<FakeBlockSender> opt = FakeBlockSender.getFrom(player);
            if (!opt.isPresent()) {
                return;
            }

            FakeBlockSender sender = opt.get();
            FakeBlock block = sender.getBlockAt(clickedBlock.toVector());

            if (block == null) {
                return;
            }

            Location placedLocation = clickedBlock.clone();
            switch (face) {
                case 2:
                    placedLocation.add(0, 0, -1);
                    break;
                case 3:
                    placedLocation.add(0, 0, 1);
                    break;
                case 4:
                    placedLocation.add(-1, 0, 0);
                    break;
                case 5:
                    placedLocation.add(1, 0, 0);
                    break;
                default:
                    return;
            }

            event.setCancelled(true);

            // Revert the block placing for the player
            if (sender.getBlockAt(placedLocation.toVector()) == null) {
                player.sendBlockChange(placedLocation, Material.AIR, (byte) 0);
                player.updateInventory();
            }
        } catch (FieldAccessException ex) {
            ex.printStackTrace();
        }
    }
}
