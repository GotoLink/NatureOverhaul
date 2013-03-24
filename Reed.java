package natureoverhaul;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.EnumPlantType;

public class Reed extends BlockMortal
{
	//=====================
	// BEGIN NATURE OVERHAUL
	//=====================
	protected float optRain = 0.8F;
	protected float optTemp = 0.8F;
	//=====================
	// END NATURE OVERHAUL
	//=====================
    protected Reed(int i, int j)
    {
        super(i, Material.plants);
        float f = 0.375F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
        setTickRandomly(true);
    }

    public void updateTick(World world, int i, int j, int k, Random random)
    {
        if (world.isAirBlock(i, j + 1, k))
        {
            int l;
            for (l = 1; world.getBlockId(i, j - l, k) == blockID; l++) { }
            if (l < 3)
            {
                int i1 = world.getBlockMetadata(i, j, k);
                if (i1 == 15)
                {
                    world.setBlock(i, j + 1, k, blockID);
                    world.setBlockMetadataWithNotify(i, j, k, 0, 2);
                }
                else
                {
                    world.setBlockMetadataWithNotify(i, j, k, i1 + 1, 2);
                }
            }
        }
		//========
		// BEGIN NATURE OVERHAUL
		//========
        if(!world.isRemote) {
        	boolean grow =  NatureOverhaul.reedGrow;
			if(grow) {
				attemptGrowth(world, i, j, k, getGrowthProb(world, i, j, k));
    }

			// ATTEMPT DEATH
			boolean death = NatureOverhaul.reedDie;
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
		
		float freq =  NatureOverhaul.reedGrowthRate;
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if((biome.rainfall == 0) || (biome.temperature > 1F)) {
				return 0F;
			} else {
				freq = (int) freq * getOptValueMult(biome.rainfall, optRain, 1F);
				freq = (int) freq * getOptValueMult(biome.temperature, optTemp, 1F);
		
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
		
		float freq = NatureOverhaul.reedDeathRate;
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if((biome.rainfall == 0) || (biome.temperature > 1F)) {
				return 1F;
			} else {
				freq = freq * getOptValueMult(biome.rainfall, optRain, 5F);
				freq = freq * getOptValueMult(biome.temperature, optTemp, 5F);
		
				return 1F / (3F * freq);
			}
		} else {
			return 1F / (3F * freq);
		}
	}
	
	/**
	* Death action; remove all reeds above and below
	*
	* @param	world
	* @param	i
	* @param	j
	* @param	k
	*/
	public void death(World world, int i, int j, int k) {
		int y = j;
		// Put y to the top so to avoid any reeds being dropped
		// since this is deat
		while(world.getBlockId(i, y + 1, k) == blockID) {
			y = y + 1;
		}
		// Now scan back down and delete
		while(world.getBlockId(i, y, k) == blockID) {
			world.setBlock(i, y, k, 0);
			y--;
		}
	}

	//========
	// END NATURE OVERHAUL
	//========
}
