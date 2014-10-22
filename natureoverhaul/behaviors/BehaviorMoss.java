package natureoverhaul.behaviors;

import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

public class BehaviorMoss extends BehaviorDeathSwitch {
    private static Block[] mossy = {Blocks.mossy_cobblestone, Blocks.stonebrick};
    private static Block[] smooth = {Blocks.cobblestone, Blocks.stonebrick};
    private static int[] mossMeta = {0, 1};
    public int growthAttemp = 15;

    public static void addMossData(Block moss, Block norm, int meta){
        ArrayUtils.add(mossy, moss);
        ArrayUtils.add(smooth, norm);
        ArrayUtils.add(mossMeta, meta);
    }

    public static boolean isMossyBlock(Block test){
        for(Block block:mossy){
            if(block==test){
                return true;
            }
        }
        return false;
    }
    @Override
	public Block getDeadBlock(Block living) {
		for(int i=0; i<mossy.length; i++){
            if(living==mossy[i]){
                return smooth[i];
            }
        }
        return null;
	}

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 15;
	}

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		//Moss grows on both stone (or only cobblestone), changing only one block
		if(world.getBlockMetadata(i, j, k) == getGrowthMeta(id)) {
            Block iD;
            int coord[];
            for (int attempt = 0; attempt < growthAttemp; attempt++) {
                coord = Utils.findRandomNeighbour(i, j, k, 1);
                iD = world.getBlock(coord[0], coord[1], coord[2]);
                if (canGrowOn(id, iD)) {
                    world.setBlock(coord[0], coord[1], coord[2], id, getGrowthMeta(id), 3);
                    return;
                }
            }
        }
	}

    public boolean canGrowOn(Block living, Block area){
        return (NatureOverhaul.INSTANCE.mossCorruptStone && area == Blocks.stone && living == mossy[0]) || area == getDeadBlock(living);
    }

    public int getGrowthMeta(Block living){
        for(int i=0; i<mossy.length; i++){
            if(living==mossy[i]){
                return mossMeta[i];
            }
        }
        return -1;
    }
}
