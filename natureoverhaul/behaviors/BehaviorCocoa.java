package natureoverhaul.behaviors;

import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BehaviorCocoa extends BehaviorDeathDisappear {
    public float minTemp = 0.7F, maxTemp = 1.5F, minRainfall = 0.8F;
    public int dropQuantity = 1;
    public BehaviorCocoa(){
        super(null, 5);
    }
	/**
	 * Checks if a cocoa can drop in this biome.
	 * 
	 * @return true if can drop in these coordinates
	 */
	public boolean canDropItem(World world, BlockPos pos) {
		// Cocoa can grow in the named biomes
		return world.isAirBlock(pos) && isValidBiome(world, pos, minTemp, maxTemp, minRainfall);
	}

	/**
	 * @return the {@link ItemStack} to emit on growth
	 */
	public ItemStack getDroppedItem() {
        //Emit cocoa dye
		return new ItemStack(Items.dye, dropQuantity, 3);
	}

	@Override
	public void grow(World world, BlockPos pos, IBlockState id) {
		//Emit item if possible
		if (canDropItem(world, pos.down())) {
			Utils.emitItem(world, pos.down(), getDroppedItem());
		}
	}
}
