package natureoverhaul.handlers;

import natureoverhaul.NOType;
import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.world.World;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
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

	@ForgeSubscribe
	public void onBoneMealUse(BonemealEvent event) {
		if (bonemeal) {
			if (applyBonemeal(event.world, event.X, event.Y, event.Z, event.ID)) {
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
	private boolean applyBonemeal(World world, int i, int j, int k, int id) {
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
