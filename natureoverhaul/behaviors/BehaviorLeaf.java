package natureoverhaul.behaviors;

import natureoverhaul.NOType;
import natureoverhaul.NatureOverhaul;
import natureoverhaul.TreeData;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BehaviorLeaf extends Behavior {
    public float minTemp = 0.7F, maxTemp = 1.0F, minRainfall = 0.4F;
    public int dropQuantity = 1;
    @Override
    public boolean hasDied(World world, int i, int j, int k, Block id) {
        return false;
    }

	@Override
	public void death(World world, int i, int j, int k, Block id) {
		//Has a chance to emit a sapling if sets accordingly
        TreeData tree = TreeData.getTree(id, world.getBlockMetadata(i, j, k), TreeData.Component.LEAF);
        if(tree!=null) {
            Block sap = tree.getBlock(TreeData.Component.SAPLING);
            if (NatureOverhaul.INSTANCE.growthType > 1 && world.rand.nextFloat() < NatureOverhaul.getGrowthProb(world, i, j, k, sap, NOType.SAPLING)) {
                Utils.emitItem(world, i, j, k, new ItemStack(sap, dropQuantity, tree.getMeta(TreeData.Component.SAPLING)));
            }
        }
		id.removedByPlayer(world, null, i, j, k, false);//Then disappear
	}

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		if (world.isAirBlock(i, j - 1, k) && isValidBiome(world, i, k, minTemp, maxTemp, minRainfall) && world.rand.nextFloat() < NatureOverhaul.getAppleGrowthProb(world, i, j, k))
			Utils.emitItem(world, i, j - 1, k, new ItemStack(Items.apple));
		if (NatureOverhaul.INSTANCE.growthType % 2 == 1 && world.isAirBlock(i, j + 1, k)) {
            TreeData tree = TreeData.getTree(id, world.getBlockMetadata(i, j, k), TreeData.Component.LEAF);
            if(tree!=null) {
                Block sap = tree.getBlock(TreeData.Component.SAPLING);
                Utils.emitItem(world, i, j + 1, k, new ItemStack(sap, dropQuantity, tree.getMeta(TreeData.Component.SAPLING)));
            }
		}
	}
}
