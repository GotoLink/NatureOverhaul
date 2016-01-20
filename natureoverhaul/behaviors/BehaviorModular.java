package natureoverhaul.behaviors;

import natureoverhaul.IBlockDeath;
import natureoverhaul.IGrowable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
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
    public void death(World world, BlockPos pos, IBlockState id) {
        deathModule.death(world, pos, id);
    }

    @Override
    public boolean hasDied(World world, BlockPos pos, IBlockState id) {
        return deathModule.hasDied(world, pos, id);
    }

    @Override
    public void grow(World world, BlockPos pos, IBlockState id) {
        growthModule.grow(world, pos, id);
    }
}
