package natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
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
	public float getGrowthRate(IBlockState state);

	/**
	 * Grows a block at given location
	 */
	public void grow(World world, BlockPos pos, IBlockState state);

	/**
	 * Called when corresponding config value has changed
	 * 
	 * @param rate
	 *            the new rate to use for randomizing death
	 */
	public void setGrowthRate(float rate);
}