package natureoverhaul;
//This enum stores limits parameters for biome behaviour
// conditions inside which the plant lives
//and outside which it dies/stops
public enum NOType {
	CACTUS(0.5F,0.5F,10F,10F),
	MOSS(10F,2F,-1F,-1F),
	FLOWER(1.5F,1.5F,2.5F,2.5F),
	//APPLE(0.4F,0.7F,1F,1F),not a block
	COCOA(0.8F,0.7F,15F,1.5F),
	LOG(-1F,-1F,6F,9F),
	MUSHROOM(1F,1F,5F,5F),
	MUSHROOMCAP(3F,3F,-1F,-1F),
	NETHERSTALK(-1F,-1F,-1F,-1F),
	REED(1F,1F,5F,5F),
	GRASS(1F,1F,0.25F,0.25F),
	CUSTOM(-1F,-1F,-1F,-1F);
	private float rainGrowth,tempGrowth,rainDeath,tempDeath;
	private NOType(float rainGrowth,float tempGrowth,float rainDeath,float tempDeath)
	{
		this.rainGrowth=rainGrowth;
		this.tempGrowth=tempGrowth;
		this.rainDeath=rainDeath;
		this.tempDeath=tempDeath;
	}
	protected float getRainGrowth(){
		return this.rainGrowth;
	}
	protected float getTempGrowth(){
		return this.tempGrowth;
	}
	protected float getRainDeath(){
		return this.rainDeath;
	}
	protected float getTempDeath(){
		return this.tempDeath;
	}
}
