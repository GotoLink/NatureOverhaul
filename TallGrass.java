package natureoverhaul;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class TallGrass
{
	//====================
	// BEGIN NATURE OVERHAUL
	//====================
    public void updateTick(World world, int i, int j, int k, Random random) {
    	if(!world.isRemote) {
    		boolean grow = NatureOverhaul.grassGrow;
			if(grow){
				grow(world, i, j, k);
			}
			
			// ATTEMPT DEATH
			boolean death = NatureOverhaul.grassDie;
			double deathProb = 1D / (0.75D);
			if(death && hasDied(world, i, j, k)) {
				death(world, i, j, k);
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
		
		float freq = NatureOverhaul.grassGrowthRate;
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if((biome.rainfall == 0) || (biome.temperature > 1.5F)) {
				return 0.015F;
			} else {
				freq = freq * getOptValueMult(biome.rainfall, optRain, 1F);
				freq = freq * getOptValueMult(biome.temperature, optTemp, 1F);
		
				return 1F / freq;
			}
		} else {
			return 1F / freq;
		}
	}
	
	/**
	* Get the death probability
	*
	* @return	Death probability
	*/
	public float getDeathProb(World world, int i, int j, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		float freq = NatureOverhaul.grassDeathRate;
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if((biome.rainfall == 0) || (biome.temperature > 1.5F)) {
				return 0.01F;
			} else {
				freq = freq * getOptValueMult(biome.rainfall, optRain, 0.25F);
				freq = freq * getOptValueMult(biome.temperature, optTemp, 0.25F);
		
				return 1F / freq;
			}
		} else {
			return 1F / freq;
		}
	}
	
	
	/**
	* Grow an item
	*/
	public void grow(World world, int i, int j, int k) {
		int scanSize = 2;
			for(int x = i - scanSize; x <= i + scanSize; x++) {
				for(int y = j - scanSize; y <= j + scanSize; y++) {
					for(int z = k - scanSize; z <= k + scanSize; z++) {
						// Check for air above grass
						if((world.getBlockId(x, y+1, z) == 0) 
							&& (world.getBlockId(x, y, z) == Block.grass.blockID)) {
							world.setBlock(x, y+1, z, blockID, 1, 3);
							return;
						}
					}
				}
		}
	}
	
	//====================
	// END NATURE OVERHAUL
	//====================
}
