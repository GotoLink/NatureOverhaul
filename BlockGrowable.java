package assets.natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
	* Grows a block:Metadata change or block added on top
	* This behavior is an example
	*/
	public void grow(World world, int i, int j, int k) {
		int metadata = world.getBlockMetadata(i, j, k);
		int id = world.getBlockId(i, j, k);
		if(metadata<15)
			world.setBlockMetadataWithNotify(i, j, k, metadata+1, 3);
		else 
			world.setBlock(i, j+1, k, id);
	}
	
}
