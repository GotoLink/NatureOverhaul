package mods.natureoverhaul;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class Leaves implements IGrowable
{

    public void updateTick(World world, int i, int j, int k, Random random)
    {
		//========
		// BEGIN NATURE OVERHAUL
		//========
		if(!world.isRemote) {
			attemptGrowth(world, i, j, k);
    }

	}
	
	private boolean attemptGrowth(World world, int i, int j, int k) {
		
		// The values for BOTH and GROWTH are odd
		boolean growSaps 	= (((int) NatureOverhaul.growthType % 2) == 1);
		boolean appleGrowth = NatureOverhaul.appleGrow;
		boolean cocoaGrowth = NatureOverhaul.cocoaGrow;
								
		// Sapling growth frequency
		float sapFreq 		= getSaplingGrowthProb(world, i, j, k);
		// Apple growth frequency
		float appleFreq 	= getAppleGrowthProb(world, i, j, k);
		// Cocoa frequency
		float cocoaFreq 	= getCocoaGrowthProb(world, i, j, k);
		
		if(growSaps && growth(sapFreq)) {
			// Try to emit a sapling
			if(world.getBlockId(i, j + 1, k) == 0) {
				emitItem(world, i, j + 1, k, new ItemStack(Block.sapling, 1,
										world.getBlockMetadata(i, j, k) % 4));
				return true;
			} 
		} else if(appleGrowth && growth(appleFreq)) {
			if((world.getBlockId(i, j - 1, k) == 0) && (appleCanGrow(world,i,j,k))) {
				emitItem(world, i, j - 1, k, new ItemStack(Item.appleRed));
				return true;
			}
		} else if((cocoaGrowth) && (growth(cocoaFreq))) {
			if((world.getBlockId(i, j - 1, k) == 0) && (cocoaCanGrow(world,i,j,k))) {
				emitItem(world, i, j - 1, k, new ItemStack(Item.dyePowder, 1, 3));
				return true;
			}
		}
		return false;
    }
	
	/**
	* Emit a specific item
	*
	* @param item	Item to emit
	*/
	private void emitItem(World world, int i, int j, int k, ItemStack item) {
		EntityItem entityitem = new EntityItem(world, i, j, k, item);
		world.spawnEntityInWorld(entityitem);
	}
	
	/** 
	* Attempts to emit an item at this location
	* Will emit either sapling, apple or cocoa
	*/
	public void grow(World world, int i, int j, int k) {
		Random rand 	= new Random();	
		int randInt 	= rand.nextInt(100);	
		
		if((cocoaCanGrow(world, i, j, k)) && (randInt < 10)) {
			emitItem(world, i, j - 1, k, new ItemStack(Item.dyePowder, 1, 3));
		} else if((appleCanGrow(world, i, j, k)) && (randInt < 25)) {
			emitItem(world, i, j - 1, k, new ItemStack(Item.appleRed));
		} else {
			emitItem(world, i, j + 1, k, new ItemStack(Block.sapling, 1,
										world.getBlockMetadata(i, j, k) % 4));
		}
	}
	
	/**
	* Check if an cocoa can grow in this biome
	*
	* @return	true if can grow here
	*/
	private boolean cocoaCanGrow(World world, int i, int j, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i,k);
		
		// Apples can grow in the named biomes
		return ((biome.temperature >= 0.7F) && (biome.temperature <= 1.5F) 
				&& (biome.rainfall >= 0.8F));
	}
	
	/**
	* Check if an apple can grow here
	* 
	* @param	world	MC World
	* @param	i		X Coord
	* @param	j		Y Coord
	* @param	k		Z Coord
	* @return	True if it can grow in these coordinates
	*/
	private boolean appleCanGrow(World world, int i, int j, int k) {
		// Get biome info
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		// Apples can grow in the named biomes
		return ((biome.temperature >= 0.7F) && (biome.temperature <= 1.0F) 
				&& (biome.rainfall > 0.4F));
	}
	/**
	* Check if there has been growth in these leaves
	*
	* @param	prob		Probability to spawn
	* @return	True if there is an item grown
	*/
	protected boolean growth(float prob) {
		// average time, in mins, between sapling pawning.
		// Remember that each tree has between 13 and 30 leaves facing "up". 
		// make number ~15 times larger if you want to do
		// "average saplings per tree" rather than "leaf block"
		// Since tickRate() is 10, we only use 6 as the mult.
		double tmp = Math.random();
		return (tmp < prob);
	}
	
	private float getSaplingGrowthProb(World world, int i, int j, int k) {
		float freq = 0.001F;
		String v=NatureOverhaul.saplingGrowthRate;
		// 20 leaves per tree
		// 3 ticks per minute
		// 180 ticks per hour
		// 4320 ticks per day
		// Super slow wants one tree to reproduce every human day
		if(v.equals("SUPERSLOW")) {
			freq = 0.0000231481481F; 
		// Slow is a new tree per tree every 6 hours 
		} else if(v.equals("SLOW")) {
			freq = 0.0000925925926F; 
		// A tree every 3 hours
		} else if(v.equals("AVERAGE")) {
			freq = 0.000185185185F; 
		//
		} else if(v.equals("FAST")) {
			freq = 0.000555555556F;
		} else if(v.equals("SUPERFAST")) {
			freq = 0.0037037037F; // Each leaf @15 mins. Each tree ~1 min: EXTREMELY FAST
		} else if(v.equals("INSANE")) {
			freq = 9.5F;
		}
		
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			freq = (int) freq * getOptValueMult(biome.rainfall, 0.8F, 10F);
			freq = (int) freq * getOptValueMult(biome.temperature, 0.8F, 0.1F);
		}
		
		return freq;
	}
	
	/**
	* Returns apple freq
	*
	* @return	apple freq
	*/
	private float getAppleGrowthProb(World world, int i, int j, int k) {
		float freq = NatureOverhaul.appleGrowthRate * 1.5F;
		
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if((biome.rainfall == 0) || (biome.temperature > 1.5F)) {
				return 0F;
			} else {
				freq = (int) freq * getOptValueMult(biome.rainfall, 0.8F, 4F);
				freq = (int) freq * getOptValueMult(biome.temperature, 0.7F, 4F);
		
				return 1F / freq;
			}
		} else {
			return 1F / freq;
		}
	}

	/**
	* Returns cocoa growth rate
	*
	* @return	float probability
	*/
	private float getCocoaGrowthProb(World world, int i, int j, int k) {
		float freq = NatureOverhaul.cocoaGrowthRate;
		
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if((biome.rainfall == 0) || (biome.temperature > 1.5F)) {
				return 0F;
			} else {
				freq = (int) freq * getOptValueMult(biome.rainfall, 1.0F, 15F);
				freq = (int) freq * getOptValueMult(biome.temperature, 1.0F, 15F);
		
				return 1F / freq;
			}
		} else {
			return 1F / freq;
		}
	}
	
	/**
	* Removes leaves from a tree when it's dying
	* Now integrated with NatureOverhaul
	* Removes leaves when forest growth is on at the rate
	* known to autoforest
	*/
    private void removeLeaves(World world, int i, int j, int k) {
		if(!world.isRemote) {
			boolean appleGrowth = NatureOverhaul.appleGrow;
			// The values for BOTH and DECAY are the higher ones
			boolean growSaps 	= ((int) NatureOverhaul.growthType > 1);
			if(growSaps) {
				// Use increased growth rate here
				if(growth(getSaplingGrowthProb(world, i, j, k) * 50)) {
					emitItem(world, i, j, k, new ItemStack(Block.sapling, 1, 
											 world.getBlockMetadata(i, j, k) % 4));
				}
			}
			
			// Check if apples grow
			if(appleGrowth) {
				if(growth(getAppleGrowthProb(world, i, j, k))) {
					emitItem(world, i, j, k, new ItemStack(Item.appleRed));
				}
			}
		} else {//========
			// END NATURE OVERHAUL
			//========
        dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k), 0);
		}
        world.setBlockToAir(i, j, k);
    }
	
	
}
