package natureoverhaul.behaviors;

import natureoverhaul.TreeUtils;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BehaviorCactus extends BehaviorStarving{
    public int maxHeight = 2, growthAttempt = 18, growthRadius = 2;
    public BehaviorCactus(){
        super(null, DeathModule.CACTUS, new Starve(9, 2));
    }

	@Override
	public void grow(World world, BlockPos pos, IBlockState id) {
		//Grow on top if too low, or on a neighbor spot
		if (TreeUtils.getTreeHeight(world, pos, id) > maxHeight) {//Find a neighbor spot for new one
			BlockPos coord;
			for (int attempt = 0; attempt < growthAttempt; attempt++) {
				coord = Utils.findRandomNeighbour(pos, growthRadius);
				if (id.getBlock().canPlaceBlockAt(world, coord)) {
					world.setBlockState(coord, id);
					return;
				}
			}
		} else {
			// Get to the top
			while (world.getBlockState(pos.up()) == id) {
				pos = pos.up();
			}
			if (world.isAirBlock(pos.up()))
				world.setBlockState(pos.up(), id);//Grow on top
		}
	}
}
