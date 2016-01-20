package natureoverhaul.events;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class LumberJackEvent extends BlockEvent {
	public final ItemStack axe;
	public final HarvestDropsEvent harvest;

	/**
	 * Called before an entire tree is going to be killed by a player with an
	 * equipped item of compatible id. The first block is already harvested, but
	 * you can cancel further processing.
	 * 
	 * @param event
	 *            the harvesting event that occurred for the first block
	 * @param it
	 *            the axe used
	 */
	public LumberJackEvent(HarvestDropsEvent event, ItemStack it) {
		super(event.world, event.pos, event.state);
		this.harvest = event;
		this.axe = it;
	}
}
