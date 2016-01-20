package natureoverhaul.behaviors;

import natureoverhaul.NOType;
import natureoverhaul.NatureOverhaul;
import natureoverhaul.TreeData;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BehaviorLeaf extends Behavior {
    public float minTemp = 0.7F, maxTemp = 1.0F, minRainfall = 0.4F;
    public int dropQuantity = 1;
    @Override
    public boolean hasDied(World world, BlockPos pos, IBlockState id) {
        return false;
    }

	@Override
	public void death(World world, BlockPos pos, IBlockState id) {
		//Has a chance to emit a sapling if sets accordingly
        TreeData tree = TreeData.getTree(id, TreeData.Component.LEAF);
        if(tree!=null) {
            IBlockState sap = tree.getState(TreeData.Component.SAPLING);
            if (NatureOverhaul.INSTANCE.growthType > 1 && world.rand.nextFloat() < NatureOverhaul.getGrowthProb(world, pos, sap, NOType.SAPLING)) {
                Utils.emitItem(world, pos, new ItemStack(sap.getBlock(), dropQuantity, sap.getBlock().damageDropped(sap)));
            }
        }
		id.getBlock().removedByPlayer(world, pos, null, false);//Then disappear
	}

	@Override
	public void grow(World world, BlockPos pos, IBlockState id) {
		if (world.isAirBlock(pos.down()) && isValidBiome(world, pos, minTemp, maxTemp, minRainfall) && world.rand.nextFloat() < NatureOverhaul.getAppleGrowthProb(world, pos))
			Utils.emitItem(world, pos.down(), new ItemStack(Items.apple));
		if (NatureOverhaul.INSTANCE.growthType % 2 == 1 && world.isAirBlock(pos.up())) {
            TreeData tree = TreeData.getTree(id, TreeData.Component.LEAF);
            if(tree!=null) {
                IBlockState sap = tree.getState(TreeData.Component.SAPLING);
                Utils.emitItem(world, pos.up(), new ItemStack(sap.getBlock(), dropQuantity, sap.getBlock().damageDropped(sap)));
            }
		}
	}
}
