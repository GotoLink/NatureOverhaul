package assets.natureoverhaul;

import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
/**
 * Event for Sapling growthType, from Clinton Alexander idea.
 * @author Olivier
 *
 */
public class SaplingGrowEventHandler {
	@ForgeSubscribe
    public void onGrowingSapling(SaplingGrowTreeEvent event){
    	if (  event.hasResult()){
    		event.setResult(Result.DENY);//Sapling doesn't grow vanilla with even growType
    	}
    }
}
