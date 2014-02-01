package natureoverhaul.behaviors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BehaviorSapling extends BehaviorDeathDisappear{

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		int maxNeighbours = 5;
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
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
	public int getStarvingRadius(World world, int i, int j, int k) {
		int radius = 2;
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		if (biome.temperature > 1F) {
			radius = 4;
		}
		if (biome.rainfall < 0.5F) {
			radius++;
		}
		return radius;
	}

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		((BlockSapling) id).func_149878_d(world, i, j, k, world.rand);
	}

}
