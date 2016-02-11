package org.originmc.blockvisualiser;

import org.bukkit.DyeColor;
import org.bukkit.Material;

public enum VisualType {

    /**
     * Represents the wall approaching claims when Spawn Tagged.
     */
    SPAWN_BORDER((player, location) -> new VisualBlockData(Material.STAINED_GLASS, DyeColor.PURPLE.getData())),

    /**
     * Represents the wall approaching claims when PVP Protected.
     */
    CLAIM_BORDER((player, location) -> new VisualBlockData(Material.STAINED_GLASS, DyeColor.PINK.getData())),

    /**
     * Represents claims shown using /faction map.
     */
    SUBCLAIM_MAP((player, location) -> new VisualBlockData(Material.LOG, (byte) 1)),

    /**
     * Represents claims shown using /faction map.
     */
    // TODO:
    // Faction faction = HCF.getPlugin().getFactionManager().getFactionAt(location);
    // return new VisualBlockData(Material.STAINED_GLASS, (faction != null ? faction.getRelation(player) : Relation.ENEMY).toDyeColour().getData());
    CLAIM_MAP((player, location) -> new VisualBlockData(Material.STAINED_GLASS, (byte) 14)),

    CREATE_CLAIM_SELECTION((player, location) -> new VisualBlockData(location.getBlockY() % 3 != 0 ? Material.GLASS : Material.GOLD_BLOCK))
    ;

    private LocationData data;

    VisualType(LocationData data) {
        this.data = data;
    }

    /**
     * Gets the {@link LocationData} instance.
     *
     * @return the filler
     */
    public LocationData blockFiller() {
        return data;
    }
}
