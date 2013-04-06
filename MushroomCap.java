package natureoverhaul;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;


public class MushroomCap extends BlockGrowable
{
	//======================
	// BEGIN NATURE OVERHAUL
	//======================
	
    public void updateTick(World world, int i, int j, int k, Random random) {
		if(!world.isRemote) {
			boolean grow = NatureOverhaul.shroomGrow;
			if(grow && (world.getBlockId(i, j + 1, k) == 0)) {
				grow(world, i, j, k);
			}
		}
	}
	
	/**
	* Get the growth probability
	*
	* @return	Growth probability
	*/
	public float getGrowthProb(World world, int i, int j, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		float freq = NatureOverhaul.shroomGrowthRate;
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if((biome.rainfall == 0) || (biome.temperature > 1F)) {
				return 0F;
			} else {
				freq = freq * getOptValueMult(biome.rainfall, optRain, 3F);
				freq = freq * getOptValueMult(biome.temperature, optTemp, 3F);
		
				return 1F / freq;
			}
		} else {
			return 1F / freq;
		}
	}

	//======================
	// END NATURE OVERHAUL
	//======================
}
