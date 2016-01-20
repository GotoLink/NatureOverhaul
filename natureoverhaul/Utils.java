package natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * @author Clinton Alexander
 */
public final class Utils {
	@Override
	public String toString() {
		return "Nature Overhaul Utility Class";
	}

	/**
	 * Emit a specific item at given location
	 *
	 * @param item
	 *            {@link ItemStack} to emit
	 */
	public static void emitItem(World world, BlockPos pos, ItemStack item) {
		if (!world.isRemote) {
			EntityItem entityitem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), item);
			world.spawnEntityInWorld(entityitem);
		}
	}

	/**
	 * Randomize given coordinates within range
	 *
	 * @param range The limiting value for the distance in each direction
	 * @return the randomized coordinates, as a BlockPos
	 */
	public static BlockPos findRandomNeighbour(BlockPos pos, int range) {
		int[] coord = new int[] { pos.getX(), pos.getY(), pos.getZ() };
        if(range>0) {
            Random rand = new Random();
            int dist;
            for (int index = 0; index < coord.length; index++) {
                dist = rand.nextInt(range + 1);
                if (rand.nextBoolean())
                    coord[index] += dist;
                else
                    coord[index] -= dist;
            }
        }
		return new BlockPos(coord[0], coord[1], coord[2]);
	}

	/**
	 * Gets the height of the lowest block of the specified {@link NOType}
	 * below the block from given coordinates.
	 *
	 * @param type
	 *            The {@link NOType} searched
	 * @return lowest block j location
	 */
	public static BlockPos getLowestType(World world, BlockPos pos, NOType type) {
		while (getType(world.getBlockState(pos.down())) == type) {
			pos = pos.down();
		}
		return pos;
	}

	/**
	 * Calculate an optimal distance coefficient. This is for cases where a
	 * larger number is desired the further you get from the optimal value. The
	 * minimum is 1, so multiplying any number "r" by the result of this
	 * operation will result in "r" if r is equal to "opt". If r is extremely
	 * far from opt, the coefficient will be extremely large
	 *
	 * @param rain
	 *            Current value
	 * @param opt
	 *            Optimal value
	 * @param tol
	 *            tolerance (lower = Higher probability)
	 * @return The modifier. Output always >= 1, where 1 is "just as likely" and
	 *         higher is "less likely"
	 */
	public static float getOptValueMult(float rain, float opt, float tol) {
		return tol * (float) Math.pow(opt - rain, 2) + 1;
	}

	/**
	 * Helper method to get a {@link NOType} from a block
	 *
	 * @param block
	 * @return The stored {@link NOType} from the given block
	 */
	public static NOType getType(IBlockState block) {
		return NatureOverhaul.getIDToTypeMapping().get(block.getBlock());
	}

	/**
	 * Check if we have at least a nearby block of corresponding id within
	 * radius of given coordinates.
	 *
	 * @param radius
	 *            Radius to check in
	 * @param ignoreSelf
	 *            If center block should be ignored
	 * @return True if at least one block is nearby
	 */
	public static boolean hasNearbyBlock(World world, BlockPos pos, IBlockState id, int radius, boolean ignoreSelf) {
		for (int y = -radius; y <= radius; y++) {
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					if (ignoreSelf && x == 0 && y == 0 && z == 0) {
						z++;
					}
					if (equal(world.getBlockState(pos.add(x, y, z)), id)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean equal(IBlockState left, IBlockState right) {
		return left.getBlock() == right.getBlock() && left.equals(right);
	}

	public static boolean equalType(Block left, Block right){
		return NatureOverhaul.getIDToTypeMapping().get(left) == NatureOverhaul.getIDToTypeMapping().get(right);
	}
}
