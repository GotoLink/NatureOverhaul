package natureoverhaul.handlers;

import java.util.ArrayList;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import natureoverhaul.NatureOverhaul;
import natureoverhaul.TreeUtils;
import natureoverhaul.Utils;
import natureoverhaul.events.LumberJackEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

/**
 * Event listener for Lumberjack system, from Clinton Alexander idea.
 * 
 * @author Olivier
 */
public class PlayerEventHandler {
	private boolean leafKill, lumber;
	public static ArrayList<Item> ids = new ArrayList<Item>();

	public PlayerEventHandler(boolean lumberjack, boolean killLeaves) {
		this.leafKill = killLeaves;
		this.lumber = lumberjack;
	}

	@SubscribeEvent
	public void onPlayerHarvesting(HarvestDropsEvent event) {
		if (lumber && event.harvester != null) {
			ItemStack itemstack = event.harvester.getCurrentEquippedItem();
			if (itemstack != null) {
				//Check for an axe in player hand
				if (ids.contains(itemstack.getItem())) {
					Block id = event.block;
					//Check for a registered log block
					if (NatureOverhaul.isLog(id, event.blockMetadata) && !MinecraftForge.EVENT_BUS.post(new LumberJackEvent(event, itemstack))) {
						if (TreeUtils.isTree(event.world, event.x, event.y, event.z, Utils.getType(id), true)) {
							//Damage axe compared to the number of blocks found
							int damage = TreeUtils.killTree(event.world, event.x, event.y, event.z, id, leafKill);
							itemstack.damageItem(damage - 1, event.harvester);
							if (itemstack.stackSize <= 0)
								event.harvester.destroyCurrentEquippedItem();
							//Drop logs
							Utils.emitItem(event.world, event.x, event.y, event.z, new ItemStack(id, damage, event.blockMetadata));
						}
					}
				}
			}
		}
	}

	public void set(boolean lumberjack, boolean killLeaves) {
		this.leafKill = killLeaves;
		this.lumber = lumberjack;
	}
}
