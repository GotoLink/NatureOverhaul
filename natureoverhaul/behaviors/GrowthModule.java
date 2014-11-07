package natureoverhaul.behaviors;

import natureoverhaul.IGrowable;
import natureoverhaul.NOType;
import natureoverhaul.TreeUtils;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * Common modules dealing with growth
 */
public enum GrowthModule implements IGrowable {
    NO_GROWTH {
        @Override
        public void grow(World world, int i, int j, int k, Block id) {
        }
    },
    FERTILIZE{
        @Override
        public void grow(World world, int i, int j, int k, Block id) {
            //Use fertilize method inside block
            if (id instanceof net.minecraft.block.IGrowable && ((net.minecraft.block.IGrowable) id).func_149851_a(world, i, j, k, world.isRemote))
                ((net.minecraft.block.IGrowable) id).func_149853_b(world, world.rand, i, j, k);
        }
    },
    MUSHROOM{
        @Override
        public void grow(World world, int i, int j, int k, Block id) {
            if(id instanceof BlockMushroom)
                ((BlockMushroom) id).func_149884_c(world, i, j, k, world.rand);
        }
    },
    SAPLING{
        @Override
        public void grow(World world, int i, int j, int k, Block id) {
            if(id instanceof BlockSapling)
                ((BlockSapling) id).func_149878_d(world, i, j, k, world.rand);
        }
    },
    TREE {
        @Override
        public void grow(World world, int i, int j, int k, Block id) {
            NOType type = Utils.getType(id);
            if (TreeUtils.isTree(world, i, j, k, type, false)) {
                TreeUtils.growTree(world, i, j, k, id, type);
            }
        }
    };

    //UNUSED
    @Override
    public float getGrowthRate() {
        return 0;
    }
    @Override
    public void setGrowthRate(float rate) {
    }
}
