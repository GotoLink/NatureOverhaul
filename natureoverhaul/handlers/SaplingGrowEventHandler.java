package natureoverhaul.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import natureoverhaul.NatureOverhaul;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;

/**
 * Event for Sapling growthType, from Clinton Alexander idea.
 *
 * @author Olivier
 */
public class SaplingGrowEventHandler {
	@SubscribeEvent
	public void onGrowingSapling(SaplingGrowTreeEvent event) {
		if (NatureOverhaul.growthType % 2 == 0) {
			event.setResult(Result.DENY);//Sapling doesn't grow vanilla with even growType
		}
	}
}
