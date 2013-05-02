package mods.natureoverhaul;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;

public class SaplingGrowEventHandler {
	@ForgeSubscribe//Event for Sapling growthType
    public void onGrowingSapling(SaplingGrowTreeEvent event){
    	if (  event.hasResult()){
    		event.setResult(Result.DENY);//Sapling doesn't grow vanilla with even growType
    	}
    }
}
