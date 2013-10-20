package assets.natureoverhaul;

/**
 * This enum stores tolerance parameters for biome behaviour, conditions inside
 * which the plant acts and outside which it stops.
 *
 * @author Olivier
 */
public enum NOType {
	//Add itself on top when growing
	CACTUS(6, 0.5F, 0.5F, 10F, 10F), REED(5, 1F, 1F, 5F, 5F),
	//Tree dependent
	COCOA(12, 15F, 15F, -1F, -1F), LOG(1, -1F, -1F, 6F, 9F), SAPLING(0, 10F, 0.1F, -1F, -1F), //only as a block
	LEAVES(9, 10F, 0.1F, -1F, -1F),
	//Spread (multiply itself when growing)
	MOSS(11, 10F, 2F, -1F, -1F), PLANT(2, 1.5F, 1.5F, 2.5F, 2.5F), MUSHROOM(7, 1F, 1F, 5F, 5F), //can grow a mushroom tree too
	MUSHROOMCAP(8, 3F, 3F, -1F, -1F), NETHERSTALK(3, -1F, -1F, -1F, -1F), GRASS(4, 1F, 1F, 0.25F, 0.25F), //for grass and mycellium
	//Use some fertilize method
	FERTILIZED(10, 1.5F, 1.5F, 2.5F, 2.5F), //Crops and stem
	//For unknown or API using
	CUSTOM(-1, -1F, -1F, -1F, -1F);
	private float rainGrowth, tempGrowth, rainDeath, tempDeath;
	private int index;

	private NOType(int id, float rainGrowth, float tempGrowth, float rainDeath, float tempDeath) {
		this.index = id;
		this.rainGrowth = rainGrowth;
		this.tempGrowth = tempGrowth;
		this.rainDeath = rainDeath;
		this.tempDeath = tempDeath;
	}

	public int getIndex() {
		return this.index;
	}

	public float getRainDeath() {
		return this.rainDeath;
	}

	public float getRainGrowth() {
		return this.rainGrowth;
	}

	public float getTempDeath() {
		return this.tempDeath;
	}

	public float getTempGrowth() {
		return this.tempGrowth;
	}
}
