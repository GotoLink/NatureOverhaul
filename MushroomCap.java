package natureoverhaul;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.EnumPlantType;

// Referenced classes of package net.minecraft.src:
//            Block, BlockFlower, Material
public class MushroomCap extends BlockGrowable
{
	//=====================
	// BEGIN NATURE OVERHAUL
	//=====================
	protected float optRain = 1.0F;
	protected float optTemp = 0.9F;
	//=====================
	// END NATURE OVERHAUL
	//=====================
    private int mushroomType;

    public MushroomCap(int i, Material material, int j, int k)
    {
        super(i, j, material);
        mushroomType = k;
        setTickRandomly(true);
    }

	//======================
	// BEGIN NATURE OVERHAUL
	//======================
	
    public void updateTick(World world, int i, int j, int k, Random random) {
		if(!world.isRemote) {
			boolean grow = NatureOverhaul.shroomGrow;
			if(grow && (world.getBlockId(i, j + 1, k) == 0)) {
				attemptGrowth(world, i, j, k);
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
