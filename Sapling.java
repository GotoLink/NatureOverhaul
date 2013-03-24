package natureoverhaul;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenForest;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;
//====================
// BEGIN NATURE OVERHAUL
//====================

public class Sapling extends BlockFlower
{
    protected Sapling(int i, int j)
    {
        super(i, j);
        float f = 0.4F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
    }

    public void updateTick(World world, int i, int j, int k, Random random)
    {
        if (world.isRemote)
        {
            return;
        }
		
		int bound = NatureOverhaul.treeGrowthRate;
        super.updateTick(world, i, j, k, random);
		// bound is *3 because metadata is now 3 times smaller
		// due to type addition
        if((world.getBlockLightValue(i, j + 1, k) >= 9) && 
			((bound == 0) || (random.nextInt(bound * 6) == 0))) { 
            int l = world.getBlockMetadata(i, j, k);
			// Added bound > 0 to ensure INSTANT is instant
			// Add 4 each time to avoid breaking the sapling
			// specific growth
			//System.out.println(l);
			if((((l & 8) == 0)) && (bound > 0)) {
                world.setBlockMetadataWithNotify(i, j, k, l | 8, 2);
			} else {
				growTree(world, i, j, k, random, false);
            }
        }
    }

	/**
	* Grow a tree ignoring death
	*/
	public void growTree(World world, int i, int j, int k, Random random) {
		growTree(world, i, j, k, random, true);
	}
	
	/**
	* Attempt to Grow a tree
	*
	* @param 	ignoreDeath		True if we should force grow the sapling
	*/
    private void growTree(World world, int i, int j, int k, Random random, boolean ignoreDeath) {
		// Check options
		Boolean sapDeathOp = NatureOverhaul.saplingDie;
		
		// Rate of big tree growth:
		int bigTreeRate = getBigTreeRate(world, i, j, k);
		
		// Choose a generator
		Object obj = null;
		//%3 as the meta data is on a %3 basis, where the 0th, 1st and 2nd index 
		// are for type, the rest is for timing tree growth
		int type = world.getBlockMetadata(i,j,k) & 3;
		if(type == 1) {
			if(random.nextInt(3) == 0) {
				obj = new WorldGenTaiga1();
			} else {
				obj = new WorldGenTaiga2(true);
			}
		} else if(type == 2) {
			obj = new WorldGenForest(true);
		} else if(random.nextInt(100) < bigTreeRate) {
			obj = new WorldGenBigTree(true);
		} else {
			if(world.getBiomeGenForCoords(i, k) == BiomeGenBase.swampland) {
				obj = new WorldGenSwamp();
			} else {
				obj = new WorldGenTrees(true);
			}
		}
		world.setBlock(i, j, k, 0); 
		
		// Ignore death of saplings
		if(ignoreDeath) {
			if(!((WorldGenerator) (obj)).generate(world, random, i, j, k)) {
				world.setBlockAndMetadata(i, j, k, blockID, type);
			}
		// Sapling has a random chance of dying instead of growing
		} else {
			boolean canDie  = sapDeathOp;
			boolean grew 	 = false;
			
			if((!canDie || !hasDied(world, i, j, k)) && 
				(grew = !((WorldGenerator) (obj)).generate(world, random, i, j, k))) {
				world.setBlockAndMetadata(i, j, k, blockID, type);
			}
		}
    }
	
	/**
	* Get the big tree rate
	*
	* @return	Big tree rate (out of 100)
	*/
	private int getBigTreeRate(World world, int i, int j, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		int temp = 10 * (int) Math.abs(0.5 - biome.temperature);
		int rain = (int) (10 * biome.rainfall);
		
		if(temp > 5 || rain == 0) {
			return 0;
		} else {
			return 22 - (temp * 2) + rain;
		}
	}
	
	/**
	* Get the death modifier
	*/
	public float getDeathProb(World world, int i, int j, int k) {
		// Every 10000 ticks, this sapling dies
		int freq = NatureOverhaul.saplingDeathRate;
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if(biome.rainfall == 0F) {
				return 1F;
			} else {
				float prob = freq * (biome.rainfall / 2);
			
				prob = prob / (1 + Math.abs(biome.temperature - 0.5F));
			
				return prob;
			}
		} else {
			return 1F / freq;
		}
	}
	
	/**
	* growth
	*/
	public float getGrowthProb(World world, int i, int j, int k) {
		return 0F;
	}
	
	/**
	* Get the privacy radius (ie; How many blocks to scan
	* for other saplings
	*
	* @return	Privacy radius
	*/
	protected int getPrivacyRadius(World world, int i, int j, int k) {
		int radius = 2;
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		if(biome.temperature > 1F) {
			radius = 4;
		}
		
		if(biome.rainfall < 0.5F) {
			radius++;
		}
		
		return radius;
	}
	
	/**
	* Get the maximum number of neighbours
	*
	* @return	Max neighbours before starvation occurs
	*/
	protected int getMaxNeighbours(World world, int i, int j, int k) {
		int max = 5;
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		if(biome.temperature <= 0F) {
			max = 1;
		} else if(biome.temperature > 1F) {
			max = 1;
		}
		
		int rainMod = (int) Math.ceil(biome.rainfall / 0.2) - 2;
		
		max = max + rainMod;
		
		if(max < 0) {
			return 0;
		} else {
			return max;
        }
    }

    public int damageDropped(int i)
    {
        return i & 3;
    }
}
//====================
// END NATURE OVERHAUL
//====================
