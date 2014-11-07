package natureoverhaul.behaviors;

import natureoverhaul.IBlockDeath;
import natureoverhaul.IGrowable;
import natureoverhaul.NatureOverhaul;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class BehaviorStarving extends BehaviorModular {
    protected final Starve starve;
    public BehaviorStarving(IGrowable growth, IBlockDeath death, Starve starvation){
        super(growth, death);
        if(starvation == null)
            throw new IllegalArgumentException("Starvation module can't be null");
        this.starve = starvation;
    }

    @Override
    public final boolean hasDied(World world, int i, int j, int k, Block id) {
        return NatureOverhaul.INSTANCE.useStarvingSystem && starve.hasStarved(world, i, j, k, id);
    }
}
