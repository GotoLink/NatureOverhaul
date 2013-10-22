package natureoverhaul.behaviors;

import natureoverhaul.NOType;
import natureoverhaul.NatureOverhaul;
import natureoverhaul.TreeUtils;
import natureoverhaul.Utils;
import net.minecraft.world.World;

public class BehaviorTree extends BehaviorRandomDeath {
	@Override
	public void death(World world, int i, int j, int k, int id) {
		NOType type = Utils.getType(id);
		if (TreeUtils.isTree(world, i, j, k, type, false)) {
			TreeUtils.killTree(world, i, Utils.getLowestTypeJ(world, i, j, k, type), k, id, type == NOType.LOG ? NatureOverhaul.decayLeaves : false);
		}
	}

	@Override
	public void grow(World world, int i, int j, int k, int id) {
		NOType type = Utils.getType(id);
		if (TreeUtils.isTree(world, i, j, k, type, false)) {
			TreeUtils.growTree(world, i, j, k, id, type);
		}
	}
}
