package natureoverhaul;

import net.minecraft.world.World;

/**
* Specifies an interface for allowing blocks
* to die on a tick basis. 
*
* @author	Clinton Alexander
*/
public interface IBlockDeath {
	
	/**
	* Check whether this block has died on this tick for any 
	* reason
	*
	* @param	world
	* @param	i
	* @param	j
	* @param	k
	* @return	True if plant has died
	*/
	abstract boolean hasDied(World world, int i, int j, int k);
	
	/**
	* Checks whether this block has starved on this tick
	* by being surrounded by too many of it's kin
	*
	* @param	world
	* @param	i
	* @param	j
	* @param	k
	* @return	True if plant has starved
	*/
	abstract boolean hasStarved(World world, int i, int j, int k);
	
	/**
	* Checks whether this block has died from natural random causes
	* 
	* @param	world
	* @param	i
	* @param	j
	* @param	k
	* @param	prob	Probability of death this tick
	* @return	True if plant has randomly died
	*/
	abstract boolean hasRandomlyDied(World world, int i, int j, int k, float prob);
	
	/**
	* The action to take upon death
	*
	* @param	world
	* @param	i
	* @param	j
	* @param	k
	*/
	abstract void death(World world, int i, int j, int k);

	abstract int getDeathRate();
}
