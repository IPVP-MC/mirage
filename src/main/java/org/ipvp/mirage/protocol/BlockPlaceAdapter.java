package org.ipvp.mirage.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.mirage.Mirage;

public class BlockPlaceAdapter extends PacketAdapter {

    public BlockPlaceAdapter(Mirage plugin) {
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

            Location clickedBlock = new Location(player.getWorld(), modifier.read(0), modifier.read(1), modifier.read(2));
            if (Mirage.getBlockSender().getBlockAt(player, clickedBlock.toVector()) != null) {
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

                if (Mirage.getBlockSender().getBlockAt(player, placedLocation.toVector()) == null) {
                    event.setCancelled(true);
                    player.sendBlockChange(placedLocation, Material.AIR, (byte) 0);
                    player.updateInventory();
                }
            }
        } catch (FieldAccessException ex) {
            ex.printStackTrace();
        }
    }
}
