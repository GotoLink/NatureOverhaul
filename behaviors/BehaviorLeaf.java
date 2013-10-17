package assets.natureoverhaul.behaviors;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import assets.natureoverhaul.NOType;
import assets.natureoverhaul.NatureOverhaul;
import assets.natureoverhaul.Utils;

public class BehaviorLeaf extends BehaviorRandomDeath {
	@Override
	public void death(World world, int i, int j, int k, int id) {
		//Has a chance to emit a sapling if sets accordingly
		int sap = NatureOverhaul.getLeafToSaplingMapping().get(id);
		if (NatureOverhaul.growthType > 1 && world.rand.nextFloat() < NatureOverhaul.getGrowthProb(world, i, j, k, sap, NOType.SAPLING)) {
			if (foundSapling(world, i, j, k, sap)) {
				Utils.emitItem(world, i, j, k, new ItemStack(sap, 1, world.getBlockMetadata(i, j, k) % 4));
			}
		}
		world.setBlockToAir(i, j, k);//Then disappear
	}

	@Override
	public void grow(World world, int i, int j, int k, int id) {
		if (world.getBlockId(i, j - 1, k) == 0 && appleCanGrow(world, i, k) && world.rand.nextFloat() < NatureOverhaul.getAppleGrowthProb(world, i, j, k))
			Utils.emitItem(world, i, j - 1, k, new ItemStack(Item.appleRed));
		if (NatureOverhaul.growthType % 2 == 1 && world.getBlockId(i, j + 1, k) == 0) {
			int sap = NatureOverhaul.getLeafToSaplingMapping().get(id);
			if (foundSapling(world, i, j, k, sap)) {
				Utils.emitItem(world, i, j + 1, k, new ItemStack(sap, 1, world.getBlockMetadata(i, j, k) % 4));
			}
		}
	}

	/**
	 * Checks if an apple can grow in this biome. Used in
	 * {@link #grow(World, int, int, int, int, NOType)} when type is leaf.
	 * 
	 * @return True if it can grow in these coordinates
	 */
	public static boolean appleCanGrow(World world, int i, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		// Apples can grow in the named biomes
		return ((biome.temperature >= 0.7F) && (biome.temperature <= 1.0F) && (biome.rainfall > 0.4F));
	}

	public static boolean foundSapling(World world, int i, int j, int k, int sap) {
		List<String> list = Arrays.asList(NatureOverhaul.getTreeIDMeta().get(sap));
		return list.contains(Integer.toString((world.getBlockMetadata(i, j, k) % 4)));
	}
}
