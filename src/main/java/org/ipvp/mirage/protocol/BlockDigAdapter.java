package org.ipvp.mirage.protocol;

import java.util.Optional;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
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

    public BlockDigAdapter(JavaPlugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        StructureModifier<EnumWrappers.PlayerDigType> digTypes = event.getPacket().getPlayerDigTypes();
        StructureModifier<BlockPosition> positions = event.getPacket().getBlockPositionModifier();

        try {
            EnumWrappers.PlayerDigType status = digTypes.read(0);
            if (status == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK || status == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {
                Player player = event.getPlayer();
                BlockPosition position = positions.read(0);
                int x = position.getX(), y = position.getY(), z = position.getZ();
                Location location = new Location(player.getWorld(), x, y, z);
                
                Optional<FakeBlockSender> sender = FakeBlockSender.getFrom(player);
                if (!sender.isPresent()) {
                    return;
                }
                
                FakeBlock visualBlock = sender.get().getBlockAt(location.toVector());
                if (visualBlock != null) {
                    event.setCancelled(true);
                    FakeBlock.Data data = visualBlock.getData();
                    if (status == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK || player.getGameMode() == GameMode.CREATIVE) {
                        player.sendBlockChange(location, data.getData());
                    }
                }
            }
        } catch (FieldAccessException ex) {
            ex.printStackTrace();
        }
    }
}
