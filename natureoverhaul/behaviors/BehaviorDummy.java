package natureoverhaul.behaviors;

import natureoverhaul.Behavior;
import net.minecraft.world.World;
/**
 * Behavior doing nothing, for error handling
 * @author Olivier
 *
 */
public class BehaviorDummy extends Behavior {
	@Override
	public void death(World world, int i, int j, int k, int id) {
	}

	@Override
	public void grow(World world, int i, int j, int k, int id) {
	}

	@Override
	public boolean hasDied(World world, int i, int j, int k, int id) {
		return false;
	}
}
