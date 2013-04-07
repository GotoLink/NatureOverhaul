package natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
* An implementation of a standard growable block
*
* @author	Clinton Alexander
* @version	1.0.0.0
*/
public abstract class BlockGrowable extends Block implements IGrowable {
	/**
	* See parent constructor
	*/
    protected BlockGrowable(int i, Material material) {
		super(i, material);
	}
	
	/**
	* Get the probability of growth occuring on this block
	*
	* @return	Probability of growth occuring on this tick
	*/
	public abstract float getGrowthProb(World world, int i, int j, int k);
	
	/**
	* Grows a block
	 * @return 
	*/
	public boolean grow(World world, int i, int j, int k) {
		int metadata = world.getBlockMetadata(i, j, k);
		int id = world.getBlockId(i, j, k);
			return true;
	}
	
}
