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


// Referenced classes of package net.minecraft.src:
//            BlockFlower, Block, World, WorldChunkManager, 
//            BiomeGenHell, ItemStack, Item

public class BlockNetherStalk extends BlockFlower
{
    protected BlockNetherStalk(int i)
    {
        super(i, 226);
        setTickRandomly(true);
        float f = 0.5F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.25F, 0.5F + f);
    }

    protected boolean canThisPlantGrowOnThisBlockID(int i)
    {
        return i == Block.slowSand.blockID;
    }

    public void updateTick(World world, int i, int j, int k, Random random)
    {
        int l = world.getBlockMetadata(i, j, k);
        if (l < 3)
        {
            WorldChunkManager worldchunkmanager = world.getWorldChunkManager();
            if (worldchunkmanager != null)
            {
                BiomeGenBase biomegenbase = worldchunkmanager.getBiomeGenAt(i, k);
                if ((biomegenbase instanceof BiomeGenHell) && random.nextInt(15) == 0)
                {
                    l++;
                    world.setBlockMetadataWithNotify(i, j, k, l);
                }
            }
        }
		//========
		// BEGIN NATURE OVERHAUL
		//========
		// ATTEMPT REPRODUCTION
		if(!world.isRemote) {
			boolean grow = NatureOverhaul.wortGrow;
			if(grow) {
				attemptGrowth(world, i, j, k);
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
