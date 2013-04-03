package natureoverhaul;
//This enum stores limits parameters for biome behaviour
//ie, conditions inside which the plant lives
//and outside which it dies
public enum OverhauledType {
	CACTUS(0.5F,0.5F,10F,10F),
	MOSS(10F,2F,-1F,-1F),
	FLOWER(1.5F,1.5F,2.5F,2.5F),
	//APPLE(4F,4F,-1F,-1F),not a block
	COCOA(15F,15F,-1F,-1F),
	LOG(-1F,-1F,6F,9F),
	MUSHROOM(1F,1F,5F,5F),
	MUSHROOMCAP(3F,3F,-1F,-1F),
	NETHERSTALK(-1F,-1F,-1F,-1F),
	REED(1F,1F,5F,5F),
	TALLGRASS(1F,1F,0.25F,0.25F);
	private float rainGrowth,tempGrowth,rainDeath,tempDeath;
	private OverhauledType(float rainGrowth,float tempGrowth,float rainDeath,float tempDeath)
	{
		this.rainGrowth=rainGrowth;
		this.tempGrowth=tempGrowth;
		this.rainDeath=rainDeath;
		this.tempDeath=tempDeath;
	}
	
}
