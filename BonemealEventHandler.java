package mods.natureoverhaul;

import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.entity.player.BonemealEvent;
/**
 * From Clinton Alexander idea.
 * @author Olivier
 *
 */
public class BonemealEventHandler {
	@ForgeSubscribe
    public void onBoneMealUse(BonemealEvent event){
    	if ( event.hasResult())
    	{
    		if( applyBonemeal(event.world, event.X, event.Y, event.Z, event.ID)){
    			event.setResult(Result.ALLOW);//BoneMeal is consumed, but doesn't act vanilla
    		}
    		else
    			event.setResult(Result.DEFAULT);
    	}
    }
	
 /**
	* Apply bonemeal to the location clicked
	* 
	* @param	id 	block ID
	* @return	true if item is applied
	*/
	private boolean applyBonemeal(World world, int i, int j, int k, int id) {
		if (NatureOverhaul.instance.isValid(id) && NatureOverhaul.instance.isGrowing(id)){
			NOType type = Utils.getType(id);
			if (type!=NOType.GRASS)
			{
				NatureOverhaul.instance.grow(world, i, j, k, id, type);
				return true;
			}
			return false;
		}
		else {
			return false;
		}
	}
}
