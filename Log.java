package mods.natureoverhaul;

import java.util.HashSet;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class Log
{
	//=====================
	// BEGIN NATURE OVERHAUL
	//=====================
	
	/**
	* Do additional damage to an axe if we're lumberjacking a tree
	*/
	private void additionalToolDamage(EntityPlayer player, int damage) {
		ItemStack itemstack = player.getCurrentEquippedItem();
		if(itemstack != null) {
			// Damage item compared to the nmber of items found
			itemstack.damageItem(damage - 1, player);
			if(itemstack.stackSize == 0) {
				player.destroyCurrentEquippedItem();
			}
		}
	}
	
    public void harvestBlock(World world, EntityPlayer entityplayer, int i, int j, int k, int l)
    {
        super.harvestBlock(world, entityplayer, i, j, k, l);
		//=======================
		// START NATURE OVERHAUL
		//=======================
		boolean lumberjack = NatureOverhaul.lumberjack;
		// Delete entire tree on block removal
		if((!world.isRemote) && (lumberjack) && (isTree(world, i, j, k, true))) {
			// Check if player is using an axe
			ItemStack itemstack = entityplayer.getCurrentEquippedItem();
	        if(itemstack != null) {
				int id = itemstack.itemID;
				// Axe IDs only
	            if((id >= 0) && (id < Item.itemsList.length) && 
				   (Item.itemsList[id] instanceof ItemAxe)) {
	            	int damage = killTree(world, i, j, k);
	    			additionalToolDamage(entityplayer,damage);
	            }
	        }
		}
		//=======================
		// END NATURE OVERHAUL
		//=======================
    }
}
