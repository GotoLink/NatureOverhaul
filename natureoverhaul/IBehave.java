package natureoverhaul;

public interface IBehave extends IGrowable, IBlockDeath {
	/**
	 * @return the optimal humidity value, where behavior speed is maximum
	 */
	public float getOptRain();

	/**
	 * @return the optimal temperature value, where behavior speed is maximum
	 */
	public float getOptTemp();
}
