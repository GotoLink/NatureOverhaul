package natureoverhaul.behaviors;

import natureoverhaul.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * Common modules dealing with death
 */
public enum DeathModule implements IBlockDeath{
    NO_DEATH {
        @Override
        public void death(World world, int i, int j, int k, Block id) {

        }
    },
    CACTUS{
        @Override
        public void death(World world, int i, int j, int k, Block id) {
            //Disappear completely from top to bottom
            int y = j;
            // Get to the top so to avoid any being dropped since this is death
            while (world.getBlock(i, y + 1, k) == id) {
                y = y + 1;
            }
            // Scan back down and delete
            while (world.getBlock(i, y, k) == id) {
                id.removedByPlayer(world, null, i, y, k, false);
                y--;
            }
        }
    },
    CROPS{
        @Override
        public void death(World world, int i, int j, int k, Block id) {
            //Ungrow, or turn to dirt if too low
            int meta = world.getBlockMetadata(i, j, k);
            if (meta >= 1)
                world.setBlockMetadataWithNotify(i, j, k, meta - 1, 2);
            else {
                world.setBlockToAir(i, j, k);
                world.setBlock(i, j - 1, k, Blocks.dirt);
            }
        }
    },
    DISAPPEAR{
        @Override
        public void death(World world, int i, int j, int k, Block id) {
            id.removedByPlayer(world, null, i, j, k, false);//turn to air
        }
    },
    TREE {
        @Override
        public void death(World world, int i, int j, int k, Block id) {
            NOType type = Utils.getType(id);
            if (TreeUtils.isTree(world, i, j, k, type, false)) {
                TreeUtils.killTree(world, i, Utils.getLowestTypeJ(world, i, j, k, type), k, id, type == NOType.LOG && NatureOverhaul.INSTANCE.decayLeaves);
            }
        }
    };

    @Override
    public boolean hasDied(World world, int i, int j, int k, Block id) {
        return false;
    }

    //UNUSED
    @Override
    public float getDeathRate() {
        return 0;
    }
    @Override
    public void setDeathRate(float rate) {
    }
}
