package natureoverhaul.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import natureoverhaul.NOType;
import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraftforge.event.entity.player.BonemealEvent;

/**
 * From Clinton Alexander idea.
 *
 * @author Olivier
 */
public class BonemealEventHandler {
	private boolean bonemeal;

	public BonemealEventHandler(boolean moddedBonemeal) {
		this.bonemeal = moddedBonemeal;
	}

	@SubscribeEvent
	public void onBoneMealUse(BonemealEvent event) {
		if (bonemeal) {
			if (applyBonemeal(event.world, event.x, event.y, event.z, event.block)) {
				event.setResult(Result.ALLOW);//BoneMeal is consumed, but doesn't act vanilla
			}
		}
	}

	public void set(boolean moddedBonemeal) {
		this.bonemeal = moddedBonemeal;
	}

	/**
	 * Apply bonemeal to the location clicked
	 *
	 * @return true if item is applied
	 */
	private boolean applyBonemeal(World world, int i, int j, int k, Block id) {
		if (NatureOverhaul.isRegistered(id) && NatureOverhaul.isGrowing(id)) {
			if (Utils.getType(id) != NOType.GRASS) {
				if(!world.isRemote){
					NatureOverhaul.grow(world, i, j, k, id);
				}
				return true;
			}
		}
		return false;
	}
}
