package natureoverhaul.behaviors;

import natureoverhaul.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Common modules dealing with death
 */
public enum DeathModule implements IBlockDeath{
    NO_DEATH {
        @Override
        public void death(World world, BlockPos pos, IBlockState id) {

        }
    },
    CACTUS{
        //Disappear completely from top to bottom
        @Override
        public void death(World world, BlockPos pos, IBlockState id) {
            // Get to the top so to avoid any being dropped since this is death
            while (world.getBlockState(pos.up()).equals(id)) {
                pos = pos.up();
            }
            // Scan back down and delete
            while (world.getBlockState(pos).equals(id)) {
                id.getBlock().removedByPlayer(world, pos, null, false);
                pos = pos.down();
            }
        }
    },
    CROPS{
        @Override
        public void death(World world, BlockPos pos, IBlockState id) {
            //Ungrow, or turn to dirt if too low
            int meta = id.getBlock().getMetaFromState(id);
            if (meta >= 1)
                world.setBlockState(pos, id.getBlock().getStateFromMeta(meta - 1), 2);
            else {
                world.setBlockToAir(pos);
                world.setBlockState(pos.down(), Blocks.dirt.getDefaultState());
            }
        }
    },
    DISAPPEAR{
        @Override
        public void death(World world, BlockPos pos, IBlockState id) {
            id.getBlock().removedByPlayer(world, pos, null, false);//turn to air
        }
    },
    TREE {
        @Override
        public void death(World world, BlockPos pos, IBlockState id) {
            NOType type = Utils.getType(id);
            if (TreeUtils.isTree(world, pos, type, false)) {
                TreeUtils.killTree(world, Utils.getLowestType(world, pos, type), id, type == NOType.LOG && NatureOverhaul.INSTANCE.decayLeaves);
            }
        }
    };

    @Override
    public boolean hasDied(World world, BlockPos pos, IBlockState id) {
        return false;
    }

    //UNUSED
    @Override
    public float getDeathRate(IBlockState state) {
        return 0;
    }
    @Override
    public void setDeathRate(float rate) {
    }
}
