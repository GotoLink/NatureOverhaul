package natureoverhaul.behaviors;

import natureoverhaul.IBlockDeath;
import natureoverhaul.IGrowable;
import net.minecraft.block.Block;
import net.minecraft.world.World;

/**
 * Behavior wrapping two modules
 */
public class BehaviorModular extends Behavior{
    protected final IBlockDeath deathModule;
    protected final IGrowable growthModule;

    public BehaviorModular(IGrowable growth, IBlockDeath death){
        this.growthModule = growth == null ? GrowthModule.NO_GROWTH : growth;
        this.deathModule = death == null ? DeathModule.NO_DEATH : death;
    }

    @Override
    public void death(World world, int i, int j, int k, Block id) {
        deathModule.death(world, i, j, k, id);
    }

    @Override
    public boolean hasDied(World world, int i, int j, int k, Block id) {
        return deathModule.hasDied(world, i, j, k, id);
    }

    @Override
    public void grow(World world, int i, int j, int k, Block id) {
        growthModule.grow(world, i, j, k, id);
    }
}
