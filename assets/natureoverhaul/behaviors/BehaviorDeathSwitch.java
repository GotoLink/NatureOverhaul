package assets.natureoverhaul.behaviors;

import net.minecraft.world.World;

public abstract class BehaviorDeathSwitch extends BehaviorStarving{
	@Override
	public void death(World world, int i, int j, int k, int id) {
		world.setBlock(i, j, k, getDeadBlockId());//turn to "dead" block
	}
	/**
	 * 
	 * @return block id to turn into on death
	 */
	public abstract int getDeadBlockId();
}
