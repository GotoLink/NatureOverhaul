package natureoverhaul;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.IPlantable;

public class Cactus extends BlockOverhauled
{
	
    /**
     * Ticks the block if it's been scheduled
     */
    public void updateTick(World world, int i, int j, int k, Random par5Random)
    {
        
		//========
		// BEGIN NATURE OVERHAUL
		//========
		if(!world.isRemote) {
			boolean grow= NatureOverhaul.cactiiGrow;
			if(grow) {
				grow(world, i, j, k);
			}
			// ATTEMPT DEATH
			boolean death=NatureOverhaul.cactiiDie;
			if(death && hasDied(world, i, j, k)) {
				death(world, i, j, k);
			}
		}
    }
	
	/**
	* Get the growth probability
	* @param	world
	* @param	i
	* @param	j
	* @param	k
	* @return	Growth probability
	*/
	public float getGrowthProb(World world, int i, int j, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		float freq = NatureOverhaul.cactiiGrowthRate;
		
		if(NatureOverhaul.biomeModifiedGrowth) {
			freq = (int) freq * getOptValueMult(biome.rainfall, optRain, 0.5F);
			freq = (int) freq * getOptValueMult(biome.temperature, optTemp, 0.5F);
		}
	
		return 1F / freq;
	}
	
	/**
	* Get the death probability
	* @param	world
	* @param	i
	* @param	j
	* @param	k
	* @return	Death probability
	*/
	public float getDeathProb(World world, int i, int j, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		
		float freq = NatureOverhaul.cactiiDeathRate;
	
		if(NatureOverhaul.biomeModifiedGrowth) {
			freq = freq * getOptValueMult(biome.rainfall, optRain, 10F);
			freq = freq * getOptValueMult(biome.temperature, optTemp, 10F);
		}
		
		return 1F / (3F * freq);
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
		// since this is death
		while(world.getBlockId(i, y + 1, k) == blockID) {
			y = y + 1;
		}
		// Now scan back down and delete
		while(world.getBlockId(i, y, k) == blockID) {
			world.setBlockToAir(i, y, k);
			y--;
		}
	}
	//========
	// END NATURE OVERHAUL
	//========
}
