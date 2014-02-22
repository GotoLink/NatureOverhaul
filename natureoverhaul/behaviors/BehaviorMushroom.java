package natureoverhaul.behaviors;

import natureoverhaul.NOType;
import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMushroom;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BehaviorMushroom extends BehaviorDeathDisappear{

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 5;
	}

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		//Small chance of having a mushroom tree, grown using vanilla method
		if (Math.random() < NatureOverhaul.getGrowthProb(world, i, j, k, Blocks.brown_mushroom_block, NOType.MUSHROOMCAP))
			((BlockMushroom) id).func_149884_c(world, i, j, k, world.rand);
		else//Grow a similar mushroom nearby
		{
			int scanSize = 3;
			int coord[];
			for (int attempt = 0; attempt < 15; attempt++) {
				coord = Utils.findRandomNeighbour(i, j, k, scanSize);
				if (id.canPlaceBlockAt(world, coord[0], coord[1], coord[2])) {
					world.setBlock(coord[0], coord[1], coord[2], id);
					return;
				}
			}
		}
	}
}
