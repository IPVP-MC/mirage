package org.originmc.blockvisualiser.block;

import org.bukkit.Material;
import org.originmc.blockvisualiser.generator.BlockGenerator;
import org.originmc.blockvisualiser.Position;

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
     * Returns the position of this block.
     *
     * @return the position
     */
    Position getPosition();

    /**
     * Returns the block generator that was used to create this block.
     *
     * @return the generator
     */
    BlockGenerator getGenerator();
}
