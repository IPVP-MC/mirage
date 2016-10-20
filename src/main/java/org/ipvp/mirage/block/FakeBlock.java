package org.ipvp.mirage.block;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.ipvp.mirage.generator.BlockGenerator;

public interface FakeBlock {

    /**
     * Represents the material and sub-data for a block. This is the
     * original data for a location and is used in checks when
     * returning blocks to their original state for players.
     */
    interface Data {

        /**
         * Returns the {@link Material} of this block.
         *
         * @return the material
         */
        Material getType();

        /**
         * Returns the data of this block.
         *
         * @return the data
         */
        byte getData();
    }

    /**
     * Returns the original (non-faked) data for this block.
     *
     * @return the data
     */
    Data getData();

    /**
     * Returns the world of this block
     * 
     * @return the world
     */
    World getWorld();
    
    /**
     * Returns the location of this block.
     *
     * @return the location
     */
    Vector getLocation();

    /**
     * Returns the block generator that was used to create this block.
     *
     * @return the generator
     */
    BlockGenerator getGenerator();
}
