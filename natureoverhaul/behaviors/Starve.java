package natureoverhaul.behaviors;

import natureoverhaul.IBlockDeath;
import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.world.World;

/**
 * The starvation algorithm
 */
public class Starve {

    public final int maxRadius = 10;
    public int maxNeighbour, starvingRadius;
    public Starve(int number, int radius){
        this.maxNeighbour = number;
        this.starvingRadius = radius;
    }

    public Starve(int number){
        this(number, 1);
    }

    /**
     * @return the maximum number of same block, a block can live with
     */
    public int getMaxNeighbour(World world, int i, int j, int k){ return maxNeighbour; }

    /**
     * @return the radius to search neighbors for, in blocks
     */
    public int getStarvingRadius(World world, int i, int j, int k) {
        return starvingRadius;
    }

    /**
     * Checks whether this block has starved on this tick by being surrounded by too many of it's kind
     *
     * @return True if block has starved
     */
    public boolean hasStarved(World world, int i, int j, int k, Block id){
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
