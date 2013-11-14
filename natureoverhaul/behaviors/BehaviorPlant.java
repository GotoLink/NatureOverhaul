package natureoverhaul.behaviors;

import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class BehaviorPlant extends BehaviorDeathDisappear {
	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 5;
	}

	@Override
	public void grow(World world, int i, int j, int k, int id) {
		int scanSize = 2;
		int coord[];
		for (int attempt = 0; attempt < 18; attempt++) {
			coord = Utils.findRandomNeighbour(i, j, k, scanSize);
			if (Block.blocksList[id].canPlaceBlockAt(world, coord[0], coord[1], coord[2]) && !world.getBlockMaterial(coord[0], coord[1], coord[2]).isLiquid()) {
				if (!isMetadataSensitive(id)) {
					world.setBlock(coord[0], coord[1], coord[2], id);
				} else {
					world.setBlock(coord[0], coord[1], coord[2], id, world.getBlockMetadata(i, j, k), 3);
				}
				return;
			}
		}
	}

	public boolean isMetadataSensitive(int id) {
		return true;
	}
}
