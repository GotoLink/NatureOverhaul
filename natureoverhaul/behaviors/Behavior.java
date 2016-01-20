package natureoverhaul.behaviors;

import natureoverhaul.IBehave;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public abstract class Behavior implements IBehave {
	private float[] data = new float[4];

	public float[] getData() {
		return this.data;
	}

	@Override
	public float getDeathRate(IBlockState state) {
		return this.data[1];
	}

	@Override
	public float getGrowthRate(IBlockState state) {
		return this.data[0];
	}

	@Override
	public float getOptRain() {
		return this.data[2];
	}

	@Override
	public float getOptTemp() {
		return this.data[3];
	}

	public Behavior setData(float... dat) {
		this.data = dat;
		return this;
	}

	@Override
	public void setDeathRate(float rate) {
		this.data[1] = rate;
	}

	@Override
	public void setGrowthRate(float rate) {
		this.data[0] = rate;
	}

    public boolean isValidBiome(World world, BlockPos pos, float minTemp, float maxTemp, float minRainfall){
        BiomeGenBase biome = world.getBiomeGenForCoords(pos);
        return biome.temperature >= minTemp && biome.temperature <= maxTemp && biome.rainfall > minRainfall;
    }
}
