package assets.natureoverhaul.handlers;

import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import assets.natureoverhaul.NatureOverhaul;

/**
 * Event for Sapling growthType, from Clinton Alexander idea.
 *
 * @author Olivier
 */
public class SaplingGrowEventHandler {
	@ForgeSubscribe
	public void onGrowingSapling(SaplingGrowTreeEvent event) {
		if (NatureOverhaul.growthType % 2 == 0) {
			event.setResult(Result.DENY);//Sapling doesn't grow vanilla with even growType
		}
	}
}
