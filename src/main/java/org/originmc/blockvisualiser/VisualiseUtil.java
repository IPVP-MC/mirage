package org.originmc.blockvisualiser;

import com.comphenix.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.originmc.blockvisualiser.block.FakeBlock;

import java.io.IOException;
import java.util.Map;

public class VisualiseUtil {

    public static void handleBlockChanges(Player player, Map<Location, FakeBlock.Data> input) throws IOException {
        if (input.isEmpty()) {
            return;
        }

        if (input.size() == 1) {
            Map.Entry<Location, FakeBlock.Data> entry = input.entrySet().iterator().next();
            FakeBlock.Data materialData = entry.getValue();
            player.sendBlockChange(entry.getKey(), materialData.getType(), materialData.getData());
            return;
        }

        Table<Chunk, Location, FakeBlock.Data> table = HashBasedTable.create();
        for (Map.Entry<Location, FakeBlock.Data> entry : input.entrySet()) {
            Location location = entry.getKey();
            if (location.getWorld().isChunkLoaded(((int) location.getX()) >> 4, ((int) location.getZ()) >> 4)) {
                table.row(entry.getKey().getChunk()).put(location, entry.getValue());
            }
        }

        for (Map.Entry<Chunk, Map<Location, FakeBlock.Data>> entry : table.rowMap().entrySet()) {
            VisualiseUtil.sendBulk(player, entry.getKey(), entry.getValue());
        }
    }

    private static void sendBulk(Player player, Chunk chunk, Map<Location, FakeBlock.Data> input) throws IOException {
        MultiBlockChangeInfo[] blockChangeInfo = new MultiBlockChangeInfo[input.size()];
        int i = 0;
        for (Map.Entry<Location, FakeBlock.Data> entry : input.entrySet()) {
            FakeBlock.Data data = entry.getValue();
            blockChangeInfo[i++] = new MultiBlockChangeInfo(entry.getKey(), WrappedBlockData.createData(data.getType()));
        }
        WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange();
        packet.setChunk(new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
        packet.setRecords(blockChangeInfo);
        packet.sendPacket(player);
    }

}
