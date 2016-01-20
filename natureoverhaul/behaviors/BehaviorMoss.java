package natureoverhaul.behaviors;

import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

public class BehaviorMoss extends BehaviorDeathSwitch {
    private static IBlockState[] mossy = {Blocks.mossy_cobblestone.getDefaultState(), Blocks.stonebrick.getStateFromMeta(BlockStoneBrick.MOSSY_META)};
    private static IBlockState[] smooth = {Blocks.cobblestone.getDefaultState(), Blocks.stonebrick.getDefaultState()};
    public int growthAttemp = 15;

    public BehaviorMoss(){
        super(null, new Starve(15));
    }

    public static void addMossData(IBlockState moss, IBlockState norm){
        mossy = ArrayUtils.add(mossy, moss);
        smooth = ArrayUtils.add(smooth, norm);
    }

    public static boolean isMossyBlock(Block test){
        for(IBlockState block:mossy){
            if(block.getBlock()==test){
                return true;
            }
        }
        return false;
    }

    @Override
	public IBlockState getDeadBlock(IBlockState living) {
		for(int i=0; i<mossy.length; i++){
            if(living.equals(mossy[i])){
                return smooth[i];
            }
        }
        return null;
	}

	@Override
	public void grow(World world, BlockPos pos, IBlockState id) {
		//Moss grows on both stone (or only cobblestone), changing only one block
		if(ArrayUtils.contains(mossy, id)) {
            IBlockState iD;
            BlockPos coord;
            for (int attempt = 0; attempt < growthAttemp; attempt++) {
                coord = Utils.findRandomNeighbour(pos, 1);
                iD = world.getBlockState(coord);
                if (canGrowOn(id, iD)) {
                    world.setBlockState(coord, id);
                    return;
                }
            }
        }
	}

    public boolean canGrowOn(IBlockState living, IBlockState area){
        return (NatureOverhaul.INSTANCE.mossCorruptStone && area == Blocks.stone && living.getBlock() == mossy[0]) || area.equals(getDeadBlock(living));
    }
}
