package org.originmc.blockvisualiser;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.originmc.blockvisualiser.generator.BlockGenerator;
import org.originmc.blockvisualiser.generator.SingleBlockGenerator;

public enum VisualType {

    /**
     * Represents the wall approaching claims when Spawn Tagged.
     */
    SPAWN_BORDER(new SingleBlockGenerator(Material.STAINED_GLASS, DyeColor.PURPLE.getData())),

    /**
     * Represents the wall approaching claims when PVP Protected.
     */
    CLAIM_BORDER(new SingleBlockGenerator(Material.STAINED_GLASS, DyeColor.PINK.getData())),

    /**
     * Represents claims shown using /faction map.
     */
    SUBCLAIM_MAP(new SingleBlockGenerator(Material.LOG, (byte) 1)),

    /**
     * Represents claims shown using /faction map.
     */
    // TODO:
    // Faction faction = HCF.getPlugin().getFactionManager().getFactionAt(location);
    // return new VisualBlockData(Material.STAINED_GLASS, (faction != null ? faction.getRelation(player) : Relation.ENEMY).toDyeColour().getData());
    CLAIM_MAP(new SingleBlockGenerator(Material.STAINED_GLASS, (byte) 14)),

    CREATE_CLAIM_SELECTION(new SingleBlockGenerator(Material.GLASS, (byte) 0))
    //player, location) -> new VisualBlockData(location.getBlockY() % 3 != 0 ? Material.GLASS : Material.GOLD_BLOCK))
    ;

    private BlockGenerator data;

    VisualType(BlockGenerator data) {
        this.data = data;
    }

    /**
     * Gets the {@link BlockGenerator} instance.
     *
     * @return the filler
     */
    public BlockGenerator blockFiller() {
        return data;
    }
}
