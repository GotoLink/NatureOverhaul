package natureoverhaul.events;

import net.minecraft.item.ItemAxe;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.world.BlockEvent;

@Cancelable
public class LumberJackEvent extends BlockEvent {
	public final ItemAxe axe;
	public final HarvestDropsEvent harvest;

	/**
	 * Called before an entire tree is going to be killed by a player with an
	 * equipped axe. The first block is already harvested, but you can cancel
	 * further processing.
	 * 
	 * @param event
	 *            the harvesting event that occurred for the first block
	 * @param it
	 *            the axe used
	 */
	public LumberJackEvent(HarvestDropsEvent event, ItemAxe it) {
		super(event.x, event.y, event.z, event.world, event.block, event.blockMetadata);
		this.harvest = event;
		this.axe = it;
	}
}
