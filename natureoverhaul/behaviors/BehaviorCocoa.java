package natureoverhaul.behaviors;

import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BehaviorCocoa extends BehaviorDeathDisappear {
	/**
	 * Checks if a cocoa can drop in this biome.
	 * 
	 * @return true if can drop in these coordinates
	 */
	public boolean canDropItem(World world, int i, int j, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		// Cocoa can grow in the named biomes
		return world.func_147439_a(i, j, k) == Blocks.air && biome.temperature >= 0.7F && biome.temperature <= 1.5F && biome.rainfall >= 0.8F;
	}

	/**
	 * @return the {@link ItemStack} to emit on growth
	 */
	public ItemStack getDroppedItem() {
		return new ItemStack(Items.dye, 1, 3);//Emit cocoa dye
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
