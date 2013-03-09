package natureoverhaul;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ForgeHooks;

public class BlockTallGrass extends BlockFlower
{
	//====================
	// BEGIN NATURE OVERHAUL
	//====================
	protected float optRain = 0.5F;
	protected float optTemp = 0.7F;
	//====================
	// END NATURE OVERHAUL
	//====================
    protected BlockTallGrass(int i, int j)
    {
        super(i, j);
        float f = 0.4F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.8F, 0.5F + f);
		setTickRandomly(true);
    }

	//====================
	// BEGIN NATURE OVERHAUL
	//====================
    public void updateTick(World world, int i, int j, int k, Random random) {
    	if(!world.isRemote) {
    		boolean grow = NatureOverhaul.grassGrow;
			if(grow){
				attemptGrowth(world, i, j, k);
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
		int metadata = world.getBlockMetadata(i, j, k);
		int id = idDropped(metadata, world.rand, 0);
		if((id >= 0) && (id < Item.itemsList.length)) {
			for(int x = i - scanSize; x <= i + scanSize; x++) {
				for(int y = j - scanSize; y <= j + scanSize; y++) {
					for(int z = k - scanSize; z <= k + scanSize; z++) {
						// Check for air above grass
						if((world.getBlockId(x, y, z) == 0) 
							&& (world.getBlockId(x, y - 1, z) == Block.grass.blockID)) {
							world.setBlockAndMetadataWithNotify(x, y, z, blockID, 1);
							return;
						}
					}
				}
			}
		}
	}
	
	//====================
	// END NATURE OVERHAUL
	//====================
}
