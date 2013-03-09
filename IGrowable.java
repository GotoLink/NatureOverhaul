package natureoverhaul;

import net.minecraft.world.World;

/**
* An interface to explain that an object is growable.
* This means that bonemeal can affect it
*
* @author	Clinton Alexander
* @version	1.0.0.0
*/

public interface IGrowable {
	/**
	* Grows a copy of this item at this location
	*
	* Overload if it requires a differnet item type
	*/
	abstract void grow(World world, int i, int j, int k);
}