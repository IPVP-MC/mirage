package org.originmc.blockvisualiser;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.originmc.blockvisualiser.protocol.BlockDigAdapter;
import org.originmc.blockvisualiser.protocol.BlockPlaceAdapter;

public class VisualiserPlugin extends JavaPlugin implements Listener {

    private static FakeBlockSender blockSender = new FakeBlockSenderImpl();

    @Override
    public void onEnable() {
        // Register protocollib listeners
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new BlockDigAdapter(this));
        manager.addPacketListener(new BlockPlaceAdapter(this));
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(blockSender::clearBlocks);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        blockSender.clearBlocks(event.getPlayer());
    }

    @EventHandler
    public void onPlayerSwitchWorld(PlayerChangedWorldEvent event) {
        blockSender.clearBlocks(event.getPlayer());
    }

    /**
     * Returns the visual block sender instance
     *
     * @return the block sender
     */
    public static FakeBlockSender getBlockSender() {
        return blockSender;
    }

}
