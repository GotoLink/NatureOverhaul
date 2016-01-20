package natureoverhaul.behaviors;

import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BehaviorPlant extends BehaviorDeathDisappear {
    public int growthRadius = 2, growthAttempt = 18;
    public BehaviorPlant(){
        super(null, 5);
    }

	@Override
	public void grow(World world, BlockPos pos, IBlockState id) {
		BlockPos coord;
		for (int attempt = 0; attempt < growthAttempt; attempt++) {
			coord = Utils.findRandomNeighbour(pos, growthRadius);
			if (id.getBlock().canPlaceBlockAt(world, coord) && !world.getBlockState(coord).getBlock().getMaterial().isLiquid()) {
                if(id.getBlock() instanceof BlockDoublePlant){
                    if(world.getBlockState(pos.down()).equals(id)){
                        pos = pos.down();
                    }
                    ((BlockDoublePlant)id).placeAt(world, coord, ((BlockDoublePlant) id).getVariant(world, pos), 3);
                    return;
                }
				world.setBlockState(coord, id);
				return;
			}
		}
	}
}
