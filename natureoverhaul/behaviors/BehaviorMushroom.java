package natureoverhaul.behaviors;

import natureoverhaul.NOType;
import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BehaviorMushroom extends BehaviorDeathDisappear{
    public int growthRadius = 3, growthAttempt = 15;
    public BehaviorMushroom(){
        super(GrowthModule.FERTILIZE, 5);
    }

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		//Small chance of having a mushroom tree, grown using vanilla method
		if (Math.random() < NatureOverhaul.getGrowthProb(world, i, j, k, Blocks.brown_mushroom_block, NOType.MUSHROOMCAP))
			super.grow(world, i, j, k, id);
		else{//Grow a similar mushroom nearby
			int coord[];
			for (int attempt = 0; attempt < growthAttempt; attempt++) {
				coord = Utils.findRandomNeighbour(i, j, k, growthRadius);
				if (id.canPlaceBlockAt(world, coord[0], coord[1], coord[2])) {
					world.setBlock(coord[0], coord[1], coord[2], id);
					return;
				}
			}
		}
	}
}
