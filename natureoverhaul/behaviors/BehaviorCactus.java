package natureoverhaul.behaviors;

import natureoverhaul.TreeUtils;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BehaviorCactus extends BehaviorStarving{

	@Override
	public void death(World world, int i, int j, int k, Block id) {
		//Disappear completely from top to bottom
		int y = j;
		// Get to the top so to avoid any being dropped since this is death
		while (world.func_147439_a(i, y + 1, k) == id) {
			y = y + 1;
		}
		// Scan back down and delete
		while (world.func_147439_a(i, y, k) == id) {
			world.func_147468_f(i, y, k);
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
		if (TreeUtils.getTreeHeight(world, i, j, k, id) > 2) {//Find a neighbor spot for new one
			int scanSize = 2;
			int[] coord;
			for (int attempt = 0; attempt < 18; attempt++) {
				coord = Utils.findRandomNeighbour(i, j, k, scanSize);
				if (id.func_149742_c(world, coord[0], coord[1], coord[2])) {
					world.func_147449_b(coord[0], coord[1], coord[2], id);
					return;
				}
			}
		} else {
			int height = j;
			// Get to the top
			while (world.func_147439_a(i, height + 1, k) == id) {
				height = height + 1;
			}
			if (world.func_147439_a(i, height + 1, k) == Blocks.air)
				world.func_147449_b(i, height + 1, k, id);//Grow on top
		}
	}
}
