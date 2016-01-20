package natureoverhaul.behaviors;

import natureoverhaul.IGrowable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class BehaviorDeathSwitch extends BehaviorStarving{
    public BehaviorDeathSwitch(IGrowable growth, Starve starvation){
        super(growth, null, starvation);
    }

	@Override
	public void death(World world, BlockPos pos, IBlockState id) {
		world.setBlockState(pos, getDeadBlock(id));//turn to "dead" block
	}
	/**
	 * @param living the block before turning to death
	 * @return block id to turn into on death
	 */
	public abstract IBlockState getDeadBlock(IBlockState living);
}
