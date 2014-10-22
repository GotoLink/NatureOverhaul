package natureoverhaul.behaviors;

import natureoverhaul.TreeUtils;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BehaviorCactus extends BehaviorStarving{
    public int maxHeight = 2, growthAttempt = 18, growthRadius = 2;
	@Override
	public void death(World world, int i, int j, int k, Block id) {
		//Disappear completely from top to bottom
		int y = j;
		// Get to the top so to avoid any being dropped since this is death
		while (world.getBlock(i, y + 1, k) == id) {
			y = y + 1;
		}
		// Scan back down and delete
		while (world.getBlock(i, y, k) == id) {
            id.removedByPlayer(world, null, i, y, k, false);
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
	public void grow(World world, int i, int j, int k, Block id) {
		//Grow on top if too low, or on a neighbor spot
		if (TreeUtils.getTreeHeight(world, i, j, k, id) > maxHeight) {//Find a neighbor spot for new one
			int[] coord;
			for (int attempt = 0; attempt < growthAttempt; attempt++) {
				coord = Utils.findRandomNeighbour(i, j, k, growthRadius);
				if (id.canPlaceBlockAt(world, coord[0], coord[1], coord[2])) {
					world.setBlock(coord[0], coord[1], coord[2], id);
					return;
				}
			}
		} else {
			int height = j;
			// Get to the top
			while (world.getBlock(i, height + 1, k) == id) {
				height = height + 1;
			}
			if (world.getBlock(i, height + 1, k) == Blocks.air)
				world.setBlock(i, height + 1, k, id);//Grow on top
		}
	}
}
