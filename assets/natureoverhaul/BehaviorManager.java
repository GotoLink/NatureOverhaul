package assets.natureoverhaul;

import java.util.HashMap;
import java.util.Map;

import assets.natureoverhaul.behaviors.BehaviorCactus;
import assets.natureoverhaul.behaviors.BehaviorCocoa;
import assets.natureoverhaul.behaviors.BehaviorCrops;
import assets.natureoverhaul.behaviors.BehaviorDummy;
import assets.natureoverhaul.behaviors.BehaviorGrass;
import assets.natureoverhaul.behaviors.BehaviorLeaf;
import assets.natureoverhaul.behaviors.BehaviorMoss;
import assets.natureoverhaul.behaviors.BehaviorMushroom;
import assets.natureoverhaul.behaviors.BehaviorPlant;
import assets.natureoverhaul.behaviors.BehaviorSapling;
import assets.natureoverhaul.behaviors.BehaviorTree;

public class BehaviorManager {
	private static Map<Integer, Behavior> blockBehaviors = new HashMap();
	private static final Behavior DUMMY = new BehaviorDummy();

	/**
	 * Used to register new behaviors
	 *
	 * @param id
	 *            the block id that behaves as specified
	 * @param behav
	 *            the behavior for the block id
	 */
	public static void setBehavior(int id, Behavior behav) {
		blockBehaviors.put(Integer.valueOf(id), behav);
	}

	static Behavior getBehavior(int id, float... data) {
		if (blockBehaviors.containsKey(Integer.valueOf(id))) {
			Behavior behavior = blockBehaviors.get(Integer.valueOf(id));
			if (data != null && data.length >= 4) {
				return behavior.setData(data);
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
			return new BehaviorDummy();
		}
	}
}
