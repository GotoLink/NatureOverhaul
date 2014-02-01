package natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public abstract class BehaviorDeathDisappear extends BehaviorStarving{
	@Override
	public void death(World world, int i, int j, int k, Block id) {
		world.func_147468_f(i, j, k);//turn to air
	}
}
