package assets.natureoverhaul.behaviors;

import assets.natureoverhaul.Behavior;
import net.minecraft.world.World;

public abstract class BehaviorRandomDeath extends Behavior {
	@Override
	public boolean hasDied(World world, int i, int j, int k, int id) {
		return false;
	}
}
