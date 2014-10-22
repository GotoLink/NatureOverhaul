package natureoverhaul.behaviors;

import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class BehaviorPlant extends BehaviorDeathDisappear {
    public int growthRadius = 2, growthAttempt = 18;
	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 5;
	}

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		int scanSize = growthRadius;
		int coord[];
		for (int attempt = 0; attempt < growthAttempt; attempt++) {
			coord = Utils.findRandomNeighbour(i, j, k, scanSize);
			if (id.canPlaceBlockAt(world, coord[0], coord[1], coord[2]) && !world.getBlock(coord[0], coord[1], coord[2]).getMaterial().isLiquid()) {
				if (!isMetadataSensitive(id)) {
					world.setBlock(coord[0], coord[1], coord[2], id);
				} else {
					world.setBlock(coord[0], coord[1], coord[2], id, world.getBlockMetadata(i, j, k), 3);
				}
				return;
			}
		}
	}

	public boolean isMetadataSensitive(Block id) {
		return true;
	}
}
