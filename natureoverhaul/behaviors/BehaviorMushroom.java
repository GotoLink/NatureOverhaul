package natureoverhaul.behaviors;

import natureoverhaul.NOType;
import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BehaviorMushroom extends BehaviorDeathDisappear{
    public int growthRadius = 3, growthAttempt = 15;
    public BehaviorMushroom(){
        super(GrowthModule.FERTILIZE, 5);
    }

	@Override
	public void grow(World world, BlockPos pos, IBlockState id) {
		//Small chance of having a mushroom tree, grown using vanilla method
		if (Math.random() < NatureOverhaul.getGrowthProb(world, pos, Blocks.brown_mushroom_block.getDefaultState(), NOType.MUSHROOMCAP))
			super.grow(world, pos, id);
		else{//Grow a similar mushroom nearby
			BlockPos coord;
			for (int attempt = 0; attempt < growthAttempt; attempt++) {
				coord = Utils.findRandomNeighbour(pos, growthRadius);
				if (id.getBlock().canPlaceBlockAt(world, coord)) {
					world.setBlockState(coord, id);
					return;
				}
			}
		}
	}
}
