package assets.natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class BehaviorGrass extends BehaviorDeathDisappear{

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 5;
	}

	@Override
	public void grow(World world, int i, int j, int k, int id) {
		//Replace surrounding dirt with grass
		int scanSize = 1;
		for (int x = i - scanSize; x <= i + scanSize; x++) {
			for (int y = j - scanSize; y <= j + scanSize; y++) {
				for (int z = k - scanSize; z <= k + scanSize; z++) {
					if (world.getBlockId(x, y, z) == Block.dirt.blockID) {
						world.setBlock(x, y, z, id);
					}
				}
			}
		}
	}
}
