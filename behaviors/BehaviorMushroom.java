package assets.natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockMushroom;
import net.minecraft.world.World;
import assets.natureoverhaul.NOType;
import assets.natureoverhaul.NatureOverhaul;
import assets.natureoverhaul.Utils;

public class BehaviorMushroom extends BehaviorDeathDisappear{

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 5;
	}

	@Override
	public void grow(World world, int i, int j, int k, int id) {
		//Small chance of having a mushroom tree, grown using vanilla method
		if (Math.random() < NatureOverhaul.getGrowthProb(world, i, j, k, id + 60, NOType.MUSHROOMCAP))
			((BlockMushroom) Block.blocksList[id]).fertilizeMushroom(world, i, j, k, world.rand);
		else//Grow a similar mushroom nearby
		{
			int scanSize = 3;
			int coord[];
			for (int attempt = 0; attempt < 15; attempt++) {
				coord = Utils.findRandomNeighbour(i, j, k, scanSize);
				if (Block.blocksList[id].canPlaceBlockAt(world, coord[0], coord[1], coord[2])) {
					world.setBlock(coord[0], coord[1], coord[2], id);
					return;
				}
			}
		}
	}
}
