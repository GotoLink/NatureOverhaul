package natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public abstract class BehaviorRandomDeath extends Behavior {
	@Override
	public boolean hasDied(World world, int i, int j, int k, Block id) {
		return false;
	}
}
