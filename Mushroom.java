package natureoverhaul;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;

public class Mushroom extends BlockFlower
{
	//=====================
	// BEGIN NATURE OVERHAUL
	//=====================
	protected float optRain = 1.0F;
	protected float optTemp = 0.9F;
	//=====================
	// END NATURE OVERHAUL
	//=====================
    protected Mushroom(int i, int j)
    {
        super(i, j);
        float f = 0.2F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
        setTickRandomly(true);
    }

    public void updateTick(World world, int i, int j, int k, Random random)
    {
		//========
		// BEGIN NATURE OVERHAUL
		//========
		boolean keepDefaultSpread = NatureOverhaul.defaultShroomSpread;
        if(keepDefaultSpread && random.nextInt(25) == 0)
        {
            byte byte0 = 4;
            int l = 5;
            for (int i1 = i - byte0; i1 <= i + byte0; i1++)
            {
                for (int k1 = k - byte0; k1 <= k + byte0; k1++)
                {
                    for (int i2 = j - 1; i2 <= j + 1; i2++)
                    {
                        if (world.getBlockId(i1, i2, k1) == blockID && --l <= 0)
                        {
                            return;
                        }
                    }
                }
            }

            int j1 = (i + random.nextInt(3)) - 1;
            int l1 = (j + random.nextInt(2)) - random.nextInt(2);
            int j2 = (k + random.nextInt(3)) - 1;
            for (int k2 = 0; k2 < 4; k2++)
            {
                if (world.isAirBlock(j1, l1, j2) && canBlockStay(world, j1, l1, j2))
                {
                    i = j1;
                    j = l1;
                    k = j2;
                }
                j1 = (i + random.nextInt(3)) - 1;
                l1 = (j + random.nextInt(2)) - random.nextInt(2);
                j2 = (k + random.nextInt(3)) - 1;
            }

            if (world.isAirBlock(j1, l1, j2) && canBlockStay(world, j1, l1, j2))
            {
                world.setBlock(j1, l1, j2, blockID);
            }
        }
		if(!world.isRemote) {
			boolean grow = NatureOverhaul.shroomTreeGrow;
			if(grow) {
				attemptGrowth(world, i, j, k);
    }
			// ATTEMPT DEATH
			boolean death = NatureOverhaul.shroomDie;
			if(death && hasDied(world, i, j, k)) {
				death(world, i, j, k);
			}
		}
		//========
		// END NATURE OVERHAUL
		//========
    }
	
	//=====================
	// BEGIN NATURE OVERHAUL
	//=====================
	
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
	public boolean fertilizeMushroom(World par1World, int par2, int par3, int par4, Random par5Random)
    {
        int var6 = par1World.getBlockMetadata(par2, par3, par4);
        par1World.setBlock(par2, par3, par4, 0);
        WorldGenBigMushroom var7 = null;

        if (this.blockID == Block.mushroomBrown.blockID)
        {
            var7 = new WorldGenBigMushroom(0);
        }
        else if (this.blockID == Block.mushroomRed.blockID)
        {
            var7 = new WorldGenBigMushroom(1);
        }

        if (var7 != null && var7.generate(par1World, par5Random, par2, par3, par4))
        {
            return true;
        }
        else
        {
            par1World.setBlockAndMetadata(par2, par3, par4, this.blockID, var6);
            return false;
        }
    }
}
