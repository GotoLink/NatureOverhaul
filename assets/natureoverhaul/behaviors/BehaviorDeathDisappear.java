package assets.natureoverhaul.behaviors;

import net.minecraft.world.World;

public abstract class BehaviorDeathDisappear extends BehaviorStarving{
	@Override
	public void death(World world, int i, int j, int k, int id) {
		world.setBlockToAir(i, j, k);//turn to air
	}
}
