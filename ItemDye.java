package natureoverhaul;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCloth;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockStem;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.entity.player.BonemealEvent;

public class ItemDye extends Item
{
    public ItemDye(int i)
    {
        super(i);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int i, int j, int k, int l)
    {
        if (!entityplayer.canPlayerEdit(i, j, k, l, itemstack))
        {
            return false;
        }
        if (itemstack.getItemDamage() == 15)
        {
            int i1 = world.getBlockId(i, j, k);

            BonemealEvent event = new BonemealEvent(entityplayer, world, i1, i, j, k);
            if (MinecraftForge.EVENT_BUS.post(event))
            {
                return false;
            }

            if (event.getResult() == Result.ALLOW)
            {
                if (!world.isRemote)
                {
                    itemstack.stackSize--;
                }
                return true;
            }
            
            if (i1 == Block.sapling.blockID)
            {
                if (!world.isRemote)
                {
                    ((BlockSapling)Block.sapling).growTree(world, i, j, k, world.rand);
                    itemstack.stackSize--;
                }
                return true;
            }
            if (i1 == Block.mushroomBrown.blockID || i1 == Block.mushroomRed.blockID)
            {
                if (!world.isRemote && ((BlockMushroom)Block.blocksList[i1]).fertilizeMushroom(world, i, j, k, world.rand))
                {
                    itemstack.stackSize--;
                }
                return true;
            }
            if (i1 == Block.melonStem.blockID || i1 == Block.pumpkinStem.blockID)
            {
                if (!world.isRemote)
                {
                    ((BlockStem)Block.blocksList[i1]).fertilizeStem(world, i, j, k);
                    itemstack.stackSize--;
                }
                return true;
            }
            if (i1 == Block.crops.blockID)
            {
                if (!world.isRemote)
                {
                    ((BlockCrops)Block.crops).fertilize(world, i, j, k);
                    itemstack.stackSize--;
                }
                return true;
            }
            if (i1 == Block.grass.blockID)
            {
                if (!world.isRemote)
                {
                    itemstack.stackSize--;
                    label0:
                    for (int j1 = 0; j1 < 128; j1++)
                    {
                        int k1 = i;
                        int l1 = j + 1;
                        int i2 = k;
                        for (int j2 = 0; j2 < j1 / 16; j2++)
                        {
                            k1 += itemRand.nextInt(3) - 1;
                            l1 += ((itemRand.nextInt(3) - 1) * itemRand.nextInt(3)) / 2;
                            i2 += itemRand.nextInt(3) - 1;
                            if (world.getBlockId(k1, l1 - 1, i2) != Block.grass.blockID || world.isBlockNormalCube(k1, l1, i2))
                            {
                                continue label0;
                            }
                        }

                        if (world.getBlockId(k1, l1, i2) != 0)
                        {
                            continue;
                        }
                        if (itemRand.nextInt(10) != 0)
                        {
                            world.setBlockAndMetadataWithNotify(k1, l1, i2, Block.tallGrass.blockID, 1);
                            continue;
                        }
                        ForgeHooks.plantGrass(world, k1, l1, i2);
                    }
                }
                return true;
			//====================
			// START NATURE OVERHAUL
			//====================
            } else if(applyBonemeal(world, i, j, k, i1)) {
				itemstack.stackSize--;
				return true;
            }
			//====================
			// END NATURE OVERHAUL
			//====================
        }
        return false;
    }

	//====================
	// START NATURE OVERHAUL
	//====================
	/**
	* Apply bonemeal to the item clicked
	* 
	* @param	id 	Item ID
	* @return	true if item is applied
	*/
	private boolean applyBonemeal(World world, int i, int j, int k, int id) {
		// Items affected; cactii, reeds, leaves, flowers and shrooms
		if(Block.blocksList[id] instanceof IGrowable) {
			((IGrowable) Block.blocksList[id]).grow(world, i, j, k);
			return true;
		} else {
			return false;
		}
	}
	//====================
	// END NATURE OVERHAUL
	//====================
}
