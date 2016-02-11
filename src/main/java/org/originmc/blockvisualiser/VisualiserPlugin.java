package org.originmc.blockvisualiser;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.originmc.blockvisualiser.protocol.BlockDigAdapter;
import org.originmc.blockvisualiser.protocol.BlockPlaceAdapter;

public class VisualiserPlugin extends JavaPlugin {

    private VisualiseHandler handler = new VisualiseHandler();

    @Override
    public void onEnable() {
        // Register protocollib listeners
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new BlockDigAdapter(this));
        manager.addPacketListener(new BlockPlaceAdapter(this));
    }

    /**
     * Returns the visual handler instance
     *
     * @return the handler
     */
    public VisualiseHandler getHandler() {
        return handler;
    }

}
