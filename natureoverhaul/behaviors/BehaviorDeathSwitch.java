package natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public abstract class BehaviorDeathSwitch extends BehaviorStarving{
	@Override
	public void death(World world, int i, int j, int k, Block id) {
		world.setBlock(i, j, k, getDeadBlock());//turn to "dead" block
	}
	/**
	 * 
	 * @return block id to turn into on death
	 */
	public abstract Block getDeadBlock();
}
