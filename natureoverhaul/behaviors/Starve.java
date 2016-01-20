package natureoverhaul.behaviors;

import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
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
    public int getMaxNeighbour(World world, BlockPos pos){ return maxNeighbour; }

    /**
     * @return the radius to search neighbors for, in blocks
     */
    public int getStarvingRadius(World world, BlockPos pos) {
        return starvingRadius;
    }

    /**
     * Checks whether this block has starved on this tick by being surrounded by too many of it's kind
     *
     * @return True if block has starved
     */
    public boolean hasStarved(World world, BlockPos pos, Block id){
        int radius = getStarvingRadius(world, pos);
        int max = getMaxNeighbour(world, pos);
        int foundNeighbours = 0;
        if (radius > 0 && radius < maxRadius) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            for (int x = i - radius; x < i + radius; x++) {
                for (int z = k - radius; z < k + radius; z++) {
                    if(world.getChunkProvider().chunkExists(x >> 4, z >> 4)) {
                        for (int y = j - radius; y < j + radius; y++) {
                            if (i != x || j != y || k != z) {
                                Block blockID = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                                if (foundNeighbours <= max && (id == blockID || (NatureOverhaul.isRegistered(blockID) && Utils.equalType(blockID, id)))) {
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
