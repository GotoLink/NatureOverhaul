package natureoverhaul.behaviors;

import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BehaviorCocoa extends BehaviorDeathDisappear {
    public float minTemp = 0.7F, maxTemp = 1.5F, minRainfall = 0.8F;
    public int dropQuantity = 1;
	/**
	 * Checks if a cocoa can drop in this biome.
	 * 
	 * @return true if can drop in these coordinates
	 */
	public boolean canDropItem(World world, int i, int j, int k) {
		// Cocoa can grow in the named biomes
		return world.getBlock(i, j, k) == Blocks.air && isValidBiome(world, i, k, minTemp, maxTemp, minRainfall);
	}

	/**
	 * @return the {@link ItemStack} to emit on growth
	 */
	public ItemStack getDroppedItem() {
		return new ItemStack(Items.dye, dropQuantity, 3);//Emit cocoa dye
	}

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 5;
	}

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		//Emit item if possible
		if (canDropItem(world, i, j - 1, k)) {
			Utils.emitItem(world, i, j - 1, k, getDroppedItem());
		}
	}
}
