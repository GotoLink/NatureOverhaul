package natureoverhaul;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;

public class Mushroom
{
	//=====================
	// BEGIN NATURE OVERHAUL
	//=====================

    public void updateTick(World world, int i, int j, int k, Random random)
    {
		//========
		// BEGIN NATURE OVERHAUL
		//========
		if(!world.isRemote) {
			boolean grow = NatureOverhaul.shroomTreeGrow;
			if(grow) {
				grow(world, i, j, k);
    }
			// ATTEMPT DEATH
			boolean death = NatureOverhaul.shroomDie;
			if(death && hasDied(world, i, j, k)) {
				death(world, i, j, k);
			}
		}
		
    }
	
	/**
	* Grow an item
	*/
	public void grow(World world, int i, int j, int k) {
		fertilizeMushroom(world, i, j, k, world.rand);
	}
	
	/**
	* Get the growth probability
	*
	* @return	Growth probability
	*/
	public float getGrowthProb(World world, int i, int j, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		float freq = NatureOverhaul.shroomTreeGrowthRate;
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if((biome.rainfall == 0) || (biome.temperature > 1F)) {
				return 0F;
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
		
		float freq = NatureOverhaul.shroomDeathRate;
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if((biome.rainfall == 0) || (biome.temperature > 1F)) {
				return 1F;
			} else {
				freq = freq * getOptValueMult(biome.rainfall, optRain, 5F);
				freq = freq * getOptValueMult(biome.temperature, optTemp, 5F);
		
				return 1F / freq;
			}
		} else {
			return 1F / freq;
		}
	}
	
	//=====================
	// END NATURE OVERHAUL
	//=====================
}
