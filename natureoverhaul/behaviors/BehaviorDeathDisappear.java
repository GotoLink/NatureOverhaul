package natureoverhaul.behaviors;

import natureoverhaul.IGrowable;

public abstract class BehaviorDeathDisappear extends BehaviorStarving{

    public BehaviorDeathDisappear(IGrowable growth, Starve starvation){
        super(growth, DeathModule.DISAPPEAR, starvation);
    }

    public BehaviorDeathDisappear(IGrowable growth, int maxNeighbour){
        super(growth, DeathModule.DISAPPEAR, new Starve(maxNeighbour));
    }
}
