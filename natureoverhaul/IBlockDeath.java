package natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.world.World;

/**
 * Specifies an interface for allowing blocks to die.
 * 
 * @author Clinton Alexander
 */
public interface IBlockDeath {
	/**
	 * The action to take upon death
	 * 
	 * @param world
	 * @param i
	 * @param j
	 * @param k
	 */
	public void death(World world, int i, int j, int k, Block id);

	/**
	 * @return The rate used for randomizing death, set to negative value to
	 *         disable
	 */
	public float getDeathRate();

	/**
	 * Check whether this block has died on this tick for any reason
	 * 
	 * @param world
	 * @param i
	 * @param j
	 * @param k
	 * @return True if plant has died
	 */
	public boolean hasDied(World world, int i, int j, int k, Block id);

	/**
	 * Called when corresponding config value has changed
	 * 
	 * @param rate
	 *            the new rate to use for randomizing death
	 */
	public void setDeathRate(float rate);
}
