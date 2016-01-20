package natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
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
	 * @param pos the position
	 */
	public void death(World world, BlockPos pos, IBlockState state);

	/**
	 * @return The rate used for randomizing death, set to negative value to
	 *         disable
	 */
	public float getDeathRate(IBlockState state);

	/**
	 * Check whether this block has died on this tick for any reason
	 * 
	 * @param world
	 * @param pos the position
	 * @return True if plant has died
	 */
	public boolean hasDied(World world, BlockPos pos, IBlockState state);

	/**
	 * Called when corresponding config value has changed
	 * 
	 * @param rate
	 *            the new rate to use for randomizing death
	 */
	public void setDeathRate(float rate);
}
