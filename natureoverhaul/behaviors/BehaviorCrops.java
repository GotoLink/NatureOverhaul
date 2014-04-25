package natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BehaviorCrops extends BehaviorStarving {
	@Override
	public void death(World world, int i, int j, int k, Block id) {
		//Ungrow, or turn to dirt if too low
		int meta = world.getBlockMetadata(i, j, k);
		if (meta >= 1)
			world.setBlockMetadataWithNotify(i, j, k, meta - 1, 2);
		else {
			world.setBlockToAir(i, j, k);
			world.setBlock(i, j - 1, k, Blocks.dirt);
		}
	}

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 6;
	}

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		//Use fertilize method inside block
		if (id instanceof IGrowable && ((IGrowable) id).func_149851_a(world, i, j, k, world.isRemote))
			((IGrowable) id).func_149853_b(world, world.rand, i, j, k);
	}
}
