package natureoverhaul;

import java.util.HashMap;
import java.util.Map;

import natureoverhaul.behaviors.*;
import net.minecraft.block.Block;

public class BehaviorManager {
	private static Map<Block, IBehave> blockBehaviors = new HashMap<Block, IBehave>();
	private static final Behavior DUMMY = new BehaviorDummy();

	/**
	 * @param id
	 *            the block id to check
	 * @return true if such block id has a registered behavior
	 */
	public static boolean isRegistered(Block id) {
		return blockBehaviors.containsKey(id);
	}

	/**
	 * Used to register new behaviors
	 * 
	 * @param id
	 *            the block id that behaves as specified
	 * @param behav
	 *            the behavior for the block id
	 */
	public static void setBehavior(Block id, IBehave behav) {
		blockBehaviors.put(id, behav);
	}

	static IBehave getBehavior(Block id, float... data) {
		if (blockBehaviors.containsKey(id)) {
			IBehave behavior = blockBehaviors.get(id);
			if (data != null && data.length >= 4 && behavior instanceof Behavior) {
				return ((Behavior) behavior).setData(data);
			} else {
				return behavior;
			}
		}
		return DUMMY;
	}

	static Behavior getBehavior(NOType type) {
		switch (type) {
		case CACTUS:
		case REED:
			return new BehaviorCactus();
		case COCOA:
			return new BehaviorCocoa();
		case FERTILIZED:
			return new BehaviorCrops();
		case GRASS:
			return new BehaviorGrass();
		case LEAVES:
			return new BehaviorLeaf();
		case LOG:
		case MUSHROOMCAP:
			return new BehaviorTree();
		case MOSS:
			return new BehaviorMoss();
		case MUSHROOM:
			return new BehaviorMushroom();
		case NETHERSTALK:
		case PLANT:
			return new BehaviorPlant();
		case SAPLING:
			return new BehaviorSapling();
		default:
			return DUMMY;
		}
	}
}
