package natureoverhaul.behaviors;

import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public abstract class BehaviorStarving extends Behavior {
    public final int maxRadius = 10;
	/**
	 * @return the maximum number of same block, a block can live with
	 */
	public abstract int getMaxNeighbour(World world, int i, int j, int k);

	/**
	 * @return the radius to search neighbors for, in blocks
	 */
	public int getStarvingRadius(World world, int i, int j, int k) {
		return 1;
	}

	@Override
	public boolean hasDied(World world, int i, int j, int k, Block id) {
		return NatureOverhaul.INSTANCE.useStarvingSystem && hasStarved(world, i, j, k, id);
	}

	/**
	 * Checks whether this block has starved on this tick by being surrounded by
	 * too many of it's kind
	 * 
	 * @return True if plant has starved
	 */
	public boolean hasStarved(World world, int i, int j, int k, Block id) {
		int radius = getStarvingRadius(world, i, j, k);
		int max = getMaxNeighbour(world, i, j, k);
		int foundNeighbours = 0;
		if (radius > 0 && radius < maxRadius) {
			for (int x = i - radius; x < i + radius; x++) {
                for (int z = k - radius; z < k + radius; z++) {
                    if(world.getChunkProvider().chunkExists(x>>4, z>>4)) {
                        for (int y = j - radius; y < j + radius; y++) {
                            if (i != x || j != y || k != z) {
                                Block blockID = world.getBlock(x, y, z);
                                if (foundNeighbours <= max && (id == blockID || (NatureOverhaul.isRegistered(blockID) && Utils.getType(blockID) == Utils.getType(id)))) {
                                    foundNeighbours++;
                                }
                            }
                        }
                    }
				}
			}
		}
		return foundNeighbours > max;
	}
}
