package natureoverhaul.events;

import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.PlayerEvent;

@Cancelable
public class FarmingEvent extends PlayerEvent {
	public final IPlantable seed;
	public final BlockPos pos;

	/**
	 * Called when player dropped seed and farming is possible for this block
	 * Cancel this event to prevent auto farming
	 * 
	 * @param player
	 *            dropping seeds
	 * @param item
	 *            the seeds
	 * @param pos
	 *            block coordinate
	 */
	public FarmingEvent(EntityPlayer player, IPlantable item, BlockPos pos) {
		super(player);
		this.seed = item;
		this.pos = pos;
	}
}
