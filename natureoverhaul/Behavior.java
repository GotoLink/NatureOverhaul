package natureoverhaul;

public abstract class Behavior implements IBehave {
	private float[] data = new float[4];

	public float[] getData() {
		return this.data;
	}

	@Override
	public float getDeathRate() {
		return this.data[1];
	}

	@Override
	public float getGrowthRate() {
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
}
