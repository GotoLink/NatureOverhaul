package natureoverhaul;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * @author Clinton Alexander
 */
public class Utils {
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
	public static void emitItem(World world, int i, int j, int k, ItemStack item) {
		if (!world.isRemote) {
			EntityItem entityitem = new EntityItem(world, i, j, k, item);
			world.spawnEntityInWorld(entityitem);
		}
	}

	/**
	 * Randomize given coordinates within range
	 *
	 * @param range
	 *            The limiting value for the distance in each direction
	 * @return An array of randomized coordinates
	 */
	public static int[] findRandomNeighbour(int i, int j, int k, int range) {
		int[] coord = new int[] { i, j, k };
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
		return coord;
	}

	/**
	 * Helper method to get leaf from a log.
	 *
	 * @param id
	 *            The log block id.
	 * @return The leaf block id corresponding to the given log block id.
	 */
	public static Block getLeafFromLog(Block id) {
		try {
			return NatureOverhaul.getLogToLeafMapping().get(id);
		} catch (NullPointerException n) {
			System.err.println("NatureOverhaul failed to find corresponding leaf to log block with id " + id + " in config file");
			return Blocks.air;
		}
	}

	/**
	 * Gets the j location of the lowest block of the specified {@link NOType}
	 * below the block from given coordinates.
	 *
	 * @param type
	 *            The {@link NOType} searched
	 * @return lowest block j location
	 */
	public static int getLowestTypeJ(World world, int i, int j, int k, NOType type) {
		int low = j;
		while (getType(world.getBlock(i, low - 1, k)) == type) {
			low--;
		}
		return low;
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
	public static NOType getType(Block block) {
		return NatureOverhaul.getIDToTypeMapping().get(block);
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
	public static boolean hasNearbyBlock(World world, int i, int j, int k, Block id, int radius, boolean ignoreSelf) {
		for (int y = -radius; y <= radius; y++) {
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					if (ignoreSelf && x == 0 && y == 0 && z == 0) {
						z++;
					}
					if (world.getBlock(i + x, j + y, k + z) == id) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
