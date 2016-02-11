package org.originmc.blockvisualiser.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.originmc.blockvisualiser.VisualBlock;
import org.originmc.blockvisualiser.VisualBlockData;
import org.originmc.blockvisualiser.VisualiserPlugin;

public class BlockDigAdapter extends PacketAdapter {

    private static final int STARTED_DIGGING = 0;
    private static final int FINISHED_DIGGING = 2;

    public BlockDigAdapter(VisualiserPlugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        StructureModifier<Integer> modifier = event.getPacket().getIntegers();

        try {
            int status = modifier.read(4);
            if (status == STARTED_DIGGING || status == FINISHED_DIGGING) {
                Player player = event.getPlayer();
                int x = modifier.read(0), y = modifier.read(1), z = modifier.read(2);
                Location location = new Location(player.getWorld(), x, y, z);
                VisualBlock visualBlock = ((VisualiserPlugin) plugin).getHandler().getVisualBlockAt(player, location);
                if (visualBlock != null) {
                    event.setCancelled(true);
                    VisualBlockData data = visualBlock.getBlockData();
                    if (status == FINISHED_DIGGING) {
                        player.sendBlockChange(location, data.getBlockType(), data.getData());
                    } else { // we check this because Blocks that broke pretty much straight away do not send a FINISHED for some weird reason.
                        /* TODO: Needed?
                        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                        if (player.getGameMode() == GameMode.CREATIVE || entityPlayer.world.getType(x, y, z).getDamage(entityPlayer, entityPlayer.world, x, y, z) >= 1.0F) {
                            player.sendBlockChange(location, data.getBlockType(), data.getData());
                        }*/
                    }
                }
            }
        } catch (FieldAccessException ex) {
            ex.printStackTrace();
        }
    }
}
