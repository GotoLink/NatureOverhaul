package natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.world.World;
/**
 * Behavior doing nothing, for error handling
 * @author Olivier
 *
 */
public class BehaviorDummy extends Behavior {
	@Override
	public void death(World world, int i, int j, int k, Block id) {
	}

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
	}

	@Override
	public boolean hasDied(World world, int i, int j, int k, Block id) {
		return false;
	}
}
