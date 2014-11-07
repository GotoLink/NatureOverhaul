package natureoverhaul.behaviors;

import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.world.World;

public class BehaviorPlant extends BehaviorDeathDisappear {
    public int growthRadius = 2, growthAttempt = 18;
    public final boolean metaSensitive;
    public BehaviorPlant(boolean metaSensitive){
        super(null, 5);
        this.metaSensitive = metaSensitive;
    }

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		int coord[];
		for (int attempt = 0; attempt < growthAttempt; attempt++) {
			coord = Utils.findRandomNeighbour(i, j, k, growthRadius);
			if (id.canPlaceBlockAt(world, coord[0], coord[1], coord[2]) && !world.getBlock(coord[0], coord[1], coord[2]).getMaterial().isLiquid()) {
                if(id instanceof BlockDoublePlant){
                    if(world.getBlock(i, j-1, k) == id){
                        j--;
                    }
                    ((BlockDoublePlant)id).func_149889_c(world, coord[0], coord[1], coord[2], world.getBlockMetadata(i, j, k), 3);
                    return;
                }
				if (!metaSensitive) {
					world.setBlock(coord[0], coord[1], coord[2], id);
				} else {
					world.setBlock(coord[0], coord[1], coord[2], id, world.getBlockMetadata(i, j, k), 3);
				}
				return;
			}
		}
	}
}
