package natureoverhaul.behaviors;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BehaviorSapling extends BehaviorDeathDisappear{
    public BehaviorSapling(){
        super(GrowthModule.FERTILIZE, BiomeSensitiveStarve.INSTANCE);
    }
    public static class BiomeSensitiveStarve extends Starve {
        public static final Starve INSTANCE = new BiomeSensitiveStarve();

        private BiomeSensitiveStarve(){
            super(5, 2);
        }

        @Override
        public int getMaxNeighbour(World world, BlockPos pos) {
            int maxNeighbours = super.getMaxNeighbour(world, pos);
            BiomeGenBase biome = world.getBiomeGenForCoords(pos);
            if (biome.temperature > 1F) {
                maxNeighbours = 1;
            }
            if (biome.temperature <= 0F)
                maxNeighbours = 1;
            maxNeighbours = maxNeighbours + (int) Math.ceil(biome.rainfall / 0.2) - 2;
            if (maxNeighbours < 0)
                maxNeighbours = 0;
            return maxNeighbours;
        }

        @Override
        public int getStarvingRadius(World world, BlockPos pos) {
            int radius = super.getStarvingRadius(world, pos);
            BiomeGenBase biome = world.getBiomeGenForCoords(pos);
            if (biome.temperature > 1F) {
                radius = 4;
            }
            if (biome.rainfall < 0.5F) {
                radius++;
            }
            return radius;
        }
    }

}
