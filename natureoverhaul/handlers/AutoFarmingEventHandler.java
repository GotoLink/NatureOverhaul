package natureoverhaul.handlers;

import natureoverhaul.Utils;
import natureoverhaul.events.FarmingEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.item.ItemTossEvent;

/**
 * Auto plant dropped seeds on fertile grounds
 * 
 * @author Olivier
 */
public class AutoFarmingEventHandler {
	private boolean auto;

	public AutoFarmingEventHandler(boolean autoFarming) {
		this.auto = autoFarming;
	}

	@ForgeSubscribe
	public void onSeedsDropped(ItemTossEvent event) {
		if (auto) {
			Item item = event.entityItem.getEntityItem().getItem();
			if (item instanceof IPlantable) {
				World world = event.player.worldObj;
				int[] info;
				int id, meta;
				for (int tries = 0; tries < 40; tries++) {
					info = Utils.findRandomNeighbour((int) event.player.posX, (int) event.player.posY - 1, (int) event.player.posZ, 3);
					if (world.getBlockId(info[0], info[1], info[2]) == (item.itemID == 116 + 256 ? Block.slowSand.blockID : Block.tilledField.blockID)
							&& world.isAirBlock(info[0], info[1] + 1, info[2])) {
						if (!MinecraftForge.EVENT_BUS.post(new FarmingEvent(event.player, (IPlantable) item, info[0], info[1], info[2]))) {
							if (item != null) {
								id = ((IPlantable) item).getPlantID(world, info[0], info[1], info[2]);
								meta = ((IPlantable) item).getPlantMetadata(world, info[0], info[1], info[2]);
								world.setBlock(info[0], info[1] + 1, info[2], id, meta, 3);
								event.setCanceled(true);
								break;
							}
						}
					}
				}
			}
		}
	}

	public void set(boolean autoFarming) {
		this.auto = autoFarming;
	}
}
