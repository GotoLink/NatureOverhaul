package assets.natureoverhaul;

import net.minecraft.world.World;

/**
 * An interface to explain that a block is growable. This means that bonemeal
 * can affect it
 * 
 * @author Clinton Alexander
 * @version 1.0.0.0
 */
public interface IGrowable {
	/**
	 * @return The rate used for randomizing growth, set to negative value to
	 *         disable
	 */
	public float getGrowthRate();

	/**
	 * Grows a block at given location
	 */
	public void grow(World world, int i, int j, int k, int id);
}