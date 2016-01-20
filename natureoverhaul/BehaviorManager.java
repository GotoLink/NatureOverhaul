package natureoverhaul;

import natureoverhaul.behaviors.*;
import net.minecraft.block.Block;

import java.util.IdentityHashMap;
import java.util.Map;

public final class BehaviorManager {
	private static Map<Block, IBehave> blockBehaviors = new IdentityHashMap<Block, IBehave>(42);
    /**
     * Behavior doing nothing, for error handling
     */
	private static final Behavior DUMMY = new BehaviorModular(GrowthModule.NO_GROWTH, DeathModule.NO_DEATH);

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
                return new BehaviorStarving(GrowthModule.FERTILIZE,DeathModule.CROPS,new Starve(6));
            case GRASS:
                return new BehaviorGrass();
            case LEAVES:
                return new BehaviorLeaf();
            case LOG:
            case MUSHROOMCAP:
                return new BehaviorModular(GrowthModule.TREE, DeathModule.TREE);
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
