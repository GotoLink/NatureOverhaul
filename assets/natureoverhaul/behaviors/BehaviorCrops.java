package assets.natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockStem;
import net.minecraft.world.World;

public class BehaviorCrops extends BehaviorStarving {
	@Override
	public void death(World world, int i, int j, int k, int id) {
		//Ungrow, or turn to dirt if too low
		int meta = world.getBlockMetadata(i, j, k);
		if (meta >= 1)
			world.setBlockMetadataWithNotify(i, j, k, meta - 1, 2);
		else {
			world.setBlockToAir(i, j, k);
			world.setBlock(i, j - 1, k, Block.dirt.blockID);
		}
	}

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 6;
	}

	@Override
	public void grow(World world, int i, int j, int k, int id) {
		//Use fertilize method inside block
		if (Block.blocksList[id] instanceof BlockStem)
			((BlockStem) Block.blocksList[id]).fertilizeStem(world, i, j, k);
		else if (Block.blocksList[id] instanceof BlockCrops)
			((BlockCrops) Block.blocksList[id]).fertilize(world, i, j, k);
	}
}
