package natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

public class BehaviorGrass extends BehaviorDeathSwitch {
    public int growthRadius = 1;
    public BehaviorGrass(){
        super(null, new Starve(5));
    }

	@Override
	public IBlockState getDeadBlock(IBlockState living) {
		return Blocks.dirt.getDefaultState();
	}

	@Override
	//Replace surrounding dirt with grass
	public void grow(World world, BlockPos pos, IBlockState id) {
		Iterator<BlockPos> positions = BlockPos.getAllInBox(pos.add(-growthRadius, -growthRadius, -growthRadius), pos.add(growthRadius, growthRadius, growthRadius)).iterator();
		while(positions.hasNext()) {
			BlockPos next = positions.next();
			if (isExtendBlockId(world.getBlockState(next).getBlock())) {
				world.setBlockState(next, id);
			}
		}
	}

	public boolean isExtendBlockId(Block id) {
		return id == Blocks.dirt;
	}
}
