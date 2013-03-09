package natureoverhaul;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;


public class BlockFlower extends BlockMortal
{
	//=====================
	// BEGIN NATURE OVERHAUL
	//=====================
	protected float optRain = 0.7F;
	protected float optTemp = 0.6F;
	//=====================
	// END NATURE OVERHAUL
	//=====================
    protected BlockFlower(int i, int j)
    {
        super(i, Material.plants);
        blockIndexInTexture = j;
        setTickRandomly(true);
        float f = 0.2F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 3F, 0.5F + f);
    }
	//========
	// BEGIN NATURE OVERHAUL
	//========
	
	public void updateTick(World world, int i, int j, int k, Random random) {
		if((!world.isRemote) && ((this.blockID == Block.plantYellow.blockID) || (this.blockID == Block.plantRed.blockID))) {
			// ATTEMPT REPRODUCTION
			//flowers = NatureOverhaul.flowers;
			boolean grow = NatureOverhaul.flowerGrow;
			if(grow) {
				attemptGrowth(world, i, j, k);
			}
			
			// ATTEMPT DEATH
			boolean death = NatureOverhaul.flowerDie;
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
		
		float freq =  NatureOverhaul.flowerGrowthRate;
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if((biome.rainfall == 0) || (biome.temperature > 1.5F)) {
				return 0F;
			} else {
				freq = freq * getOptValueMult(biome.rainfall, optRain, 1.5F);
				freq = freq * getOptValueMult(biome.temperature, optTemp, 1.5F);
			
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
		
		float freq =  NatureOverhaul.flowerDeathRate;
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			if((biome.rainfall == 0) || (biome.temperature > 1.5F)) {
				return 1F;
			} else {
				freq = freq * getOptValueMult(biome.rainfall, optRain, 2.5F);
				freq = freq * getOptValueMult(biome.temperature, optTemp, 2.5F);
			
				return 1F / freq;
			}
		} else {
			return 1F / freq;
		}
	}
	//========
	// END NATURE OVERHAUL
	//========
}
