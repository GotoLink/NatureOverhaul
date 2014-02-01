package natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BehaviorGrass extends BehaviorDeathSwitch {
	@Override
	public Block getDeadBlock() {
		return Blocks.dirt;
	}

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 5;
	}

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		//Replace surrounding dirt with grass
		int scanSize = 1;
		for (int x = i - scanSize; x <= i + scanSize; x++) {
			for (int y = j - scanSize; y <= j + scanSize; y++) {
				for (int z = k - scanSize; z <= k + scanSize; z++) {
					if (isExtendBlockId(world.func_147439_a(x, y, z))) {
						world.func_147449_b(x, y, z, id);
					}
				}
			}
		}
	}

	public boolean isExtendBlockId(Block id) {
		return id == Blocks.dirt;
	}
}
