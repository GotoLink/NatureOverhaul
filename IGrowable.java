package natureoverhaul;

import net.minecraft.world.World;

/**
* An interface to explain that a block is growable.
* This means that bonemeal can affect it
*
* @author	Clinton Alexander
* @version	1.0.0.0
*/

public interface IGrowable {
	/**
	* Grows a copy of this block at this location
	*
	*/
	abstract void grow(World world, int i, int j, int k);

	abstract int getGrowthRate();
}