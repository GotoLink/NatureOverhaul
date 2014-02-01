package natureoverhaul.events;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.PlayerEvent;

@Cancelable
public class FarmingEvent extends PlayerEvent {
	public IPlantable seed;
	public int posX;
	public int posY;
	public int posZ;

	/**
	 * Called when player dropped seed and farming is possible for this block
	 * Cancel this event to prevent auto farming
	 * 
	 * @param player
	 *            dropping seeds
	 * @param item
	 *            the seeds
	 * @param i
	 *            block coordinate
	 * @param j
	 *            block coordinate
	 * @param k
	 *            block coordinate
	 */
	public FarmingEvent(EntityPlayer player, IPlantable item, int i, int j, int k) {
		super(player);
		this.seed = item;
		this.posX = i;
		this.posY = j;
		this.posZ = k;
	}
}
