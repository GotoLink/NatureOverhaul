package assets.natureoverhaul;

/**
 *This enum stores tolerance parameters for biome behaviour, conditions inside which
 * the plant acts and outside which it stops.
 * @author Olivier
 *
 */
public enum NOType {
	//Add itself on top when growing
	CACTUS(0.5F,0.5F,10F,10F),
	REED(1F,1F,5F,5F),
	//Tree dependent
	APPLE(4F,4F,-1F,-1F),//not a block !
	COCOA(15F,15F,-1F,-1F),
	LOG(-1F,-1F,6F,9F),
	SAPLING(10F,0.1F,-1F,-1F),//only as a block for now
	LEAVES(10F,0.1F,-1F,-1F),
	//Spread (multiply itself when growing)
	MOSS(10F,2F,-1F,-1F),
	PLANT(1.5F,1.5F,2.5F,2.5F),
	MUSHROOM(1F,1F,5F,5F),//can grow a mushroom tree too
	MUSHROOMCAP(3F,3F,-1F,-1F),
	NETHERSTALK(-1F,-1F,-1F,-1F),
	GRASS(1F,1F,0.25F,0.25F),//For grass and mycellium
	//Use some fertilize method
	FERTILIZED(1.5F,1.5F,2.5F,2.5F),//Crops and stem
	//For unknown or API using
	CUSTOM(-1F,-1F,-1F,-1F);
	private float rainGrowth,tempGrowth,rainDeath,tempDeath;
	private NOType(float rainGrowth,float tempGrowth,float rainDeath,float tempDeath)
	{
		this.rainGrowth=rainGrowth;
		this.tempGrowth=tempGrowth;
		this.rainDeath=rainDeath;
		this.tempDeath=tempDeath;
	}
	public float getRainGrowth(){
		return this.rainGrowth;
	}
	public float getTempGrowth(){
		return this.tempGrowth;
	}
	public float getRainDeath(){
		return this.rainDeath;
	}
	public float getTempDeath(){
		return this.tempDeath;
	}
}
