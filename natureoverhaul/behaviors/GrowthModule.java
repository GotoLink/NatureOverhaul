package natureoverhaul.behaviors;

import natureoverhaul.IGrowable;
import natureoverhaul.NOType;
import natureoverhaul.TreeUtils;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Common modules dealing with growth
 */
public enum GrowthModule implements IGrowable {
    NO_GROWTH {
        @Override
        public void grow(World world, BlockPos pos, IBlockState id) {
        }
    },
    FERTILIZE{
        @Override
        public void grow(World world, BlockPos pos, IBlockState id) {
            //Use fertilize method inside block
            if (id.getBlock() instanceof net.minecraft.block.IGrowable && ((net.minecraft.block.IGrowable) id.getBlock()).canGrow(world, pos, id, world.isRemote))
                ((net.minecraft.block.IGrowable) id.getBlock()).grow(world, world.rand, pos, id);
        }
    },
    SAPLING{
        @Override
        public void grow(World world, BlockPos pos, IBlockState id) {
            if(id.getBlock() instanceof BlockSapling)
                ((BlockSapling) id).generateTree(world, pos, id, world.rand);
        }
    },
    TREE {
        @Override
        public void grow(World world, BlockPos pos, IBlockState id) {
            NOType type = Utils.getType(id);
            if (TreeUtils.isTree(world, pos, type, false)) {
                TreeUtils.growTree(world, pos, id, type);
            }
        }
    };

    //UNUSED
    @Override
    public float getGrowthRate(IBlockState state) {
        return 0;
    }
    @Override
    public void setGrowthRate(float rate) {
    }
}
