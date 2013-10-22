package natureoverhaul.behaviors;

import natureoverhaul.TreeUtils;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class BehaviorCactus extends BehaviorStarving{

	@Override
	public void death(World world, int i, int j, int k, int id) {
		//Disappear completely from top to bottom
		int y = j;
		// Get to the top so to avoid any being dropped since this is death
		while (world.getBlockId(i, y + 1, k) == id) {
			y = y + 1;
		}
		// Scan back down and delete
		while (world.getBlockId(i, y, k) == id) {
			world.setBlockToAir(i, y, k);
			y--;
		}
	}

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 9;
	}

	@Override
	public int getStarvingRadius(World world, int i, int j, int k) {
		return 2;
	}

	@Override
	public void grow(World world, int i, int j, int k, int id) {
		//Grow on top if too low, or on a neighbor spot
		if (TreeUtils.getTreeHeight(world, i, j, k, id) > 2) {//Find a neighbor spot for new one
			int scanSize = 2;
			int[] coord;
			for (int attempt = 0; attempt < 18; attempt++) {
				coord = Utils.findRandomNeighbour(i, j, k, scanSize);
				if (Block.blocksList[id].canPlaceBlockAt(world, coord[0], coord[1], coord[2])) {
					world.setBlock(coord[0], coord[1], coord[2], id);
					return;
				}
			}
		} else {
			int height = j;
			// Get to the top
			while (world.getBlockId(i, height + 1, k) == id) {
				height = height + 1;
			}
			if (world.getBlockId(i, height + 1, k) == 0)
				world.setBlock(i, height + 1, k, id);//Grow on top
		}
	}
}
