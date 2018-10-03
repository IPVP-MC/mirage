package org.ipvp.mirage.protocol;

import java.util.Optional;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.mirage.FakeBlockSender;
import org.ipvp.mirage.block.FakeBlock;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;

public class BlockDigAdapter extends PacketAdapter {

    private static final int STARTED_DIGGING = 0;
    private static final int FINISHED_DIGGING = 2;

    public BlockDigAdapter(JavaPlugin plugin) {
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
                
                Optional<FakeBlockSender> sender = FakeBlockSender.getFrom(player);
                if (!sender.isPresent()) {
                    return;
                }
                
                FakeBlock visualBlock = sender.get().getBlockAt(location.toVector());
                if (visualBlock != null) {
                    event.setCancelled(true);
                    FakeBlock.Data data = visualBlock.getData();
                    if (status == FINISHED_DIGGING || player.getGameMode() == GameMode.CREATIVE) {
                        player.sendBlockChange(location, data.getData());
                    }
                }
            }
        } catch (FieldAccessException ex) {
            ex.printStackTrace();
        }
    }
}
