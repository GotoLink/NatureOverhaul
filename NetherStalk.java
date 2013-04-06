package natureoverhaul;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenHell;
import net.minecraft.world.biome.WorldChunkManager;

public class NetherStalk 
{
    public void updateTick(World world, int i, int j, int k, Random random)
    {
		//========
		// BEGIN NATURE OVERHAUL
		//========
		// ATTEMPT REPRODUCTION
		if(!world.isRemote) {
			boolean grow = NatureOverhaul.wortGrow;
			if(grow) {
				grow(world, i, j, k);
			}
		
			// ATTEMPT DEATH
			boolean death = NatureOverhaul.wortDie;
			if(death && hasDied(world, i, j, k)) {
				death(world, i, j, k);
			}
		}
		//========
		// END NATURE OVERHAUL
		//========
        super.updateTick(world, i, j, k, random);
    }
	//========
	// BEGIN NATURE OVERHAUL
	//========
	
	/**
	* Get the growth probability
	*
	* @return	Growth probability
	*/
	public float getGrowthProb(World world, int i, int j, int k) {
		float freq =  NatureOverhaul.wortGrowthRate;
		
		return 1F / freq;
	}
	
	/**
	* Get the death probability
	*
	* @return	Death probability
	*/
	public float getDeathProb(World world, int i, int j, int k) {
		float freq =  NatureOverhaul.wortDeathRate;
		return 1F / freq;
	}
	//========
	// END NATURE OVERHAUL
	//========
}
