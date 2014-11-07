package natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BehaviorGrass extends BehaviorDeathSwitch {
    public int growthRadius = 1;
    public BehaviorGrass(){
        super(null, new Starve(5));
    }

	@Override
	public Block getDeadBlock(Block living) {
		return Blocks.dirt;
	}

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		//Replace surrounding dirt with grass
		int scanSize = growthRadius;
		for (int x = i - scanSize; x <= i + scanSize; x++) {
			for (int y = j - scanSize; y <= j + scanSize; y++) {
				for (int z = k - scanSize; z <= k + scanSize; z++) {
					if (isExtendBlockId(world.getBlock(x, y, z))) {
						world.setBlock(x, y, z, id);
					}
				}
			}
		}
	}

	public boolean isExtendBlockId(Block id) {
		return id == Blocks.dirt;
	}
}
