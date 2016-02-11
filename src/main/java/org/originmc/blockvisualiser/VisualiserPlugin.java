package org.originmc.blockvisualiser;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.originmc.blockvisualiser.protocol.BlockDigAdapter;
import org.originmc.blockvisualiser.protocol.BlockPlaceAdapter;

public class VisualiserPlugin extends JavaPlugin {

    private FakeBlockSender blockSender = new FakeBlockSenderImpl();

    @Override
    public void onEnable() {
        // Register protocollib listeners
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new BlockDigAdapter(this));
        manager.addPacketListener(new BlockPlaceAdapter(this));
    }

    /**
     * Returns the visual block sender instance
     *
     * @return the block sender
     */
    public FakeBlockSender getBlockSender() {
        return blockSender;
    }

}
