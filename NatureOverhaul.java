package natureoverhaul;
//Author: Clinton Alexander
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockMushroomCap;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.BlockNetherStalk;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "NatureOverhaul", name = "Nature Overhaul", version = "0.0.1")
@NetworkMod(clientSideRequired = false, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = { "NatureOverhaul" }, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = { "NatureOverhaul" }, packetHandler = ServerPacketHandler.class))
public class NatureOverhaul implements ITickHandler{
	@Instance ("NatureOverhaul")
	public static NatureOverhaul instance;
	public static Boolean autoSapling=false,lumberjack=false;
	public static Boolean defaultShroomSpread=true,biomeModifiedGrowth=true;
	public static Boolean wildAnimalsBreed=true;
	public static int wildAnimalBreedRate=0,reproductionRate=0;
	public static int growthType=0;//For sapling-> tree behaviour
	public static boolean[] dieSets=new boolean[10],growSets=new boolean[13];
	public static int[] deathRates=new int[10],growthRates=new int[13];
	private static WorldServer world;
	protected int updateLCG = (new Random()).nextInt();

// Default labels
	public static String[] labels = {"AVERAGE", "FAST", "SUPERFAST", "INSANE", "SUPERSLOW", "SLOW"};
	public static HashMap<String,Integer> stringToRateMapping = new HashMap();//TODO: Use this ?
	static{
	stringToRateMapping.put("INSANE", 5);
	stringToRateMapping.put("SUPERFAST", 250);
	stringToRateMapping.put("FAST", 1250);
	stringToRateMapping.put("AVERAGE", 2500);
	stringToRateMapping.put("SLOW", 5000);
	stringToRateMapping.put("SUPERSLOW", 10000);
	}
	public static HashMap<Integer,NOType> IDToTypeMapping = new HashMap();
	public static HashMap<Integer,Boolean> IDToGrowingMapping = new HashMap(),IDToDyingMapping = new HashMap();
	public static HashMap<Integer,Float> IDToOptTempMapping = new HashMap(),IDToOptRainMapping = new HashMap();
	public static HashMap<Integer,Integer> IDToGrowthRateMapping= new HashMap(),IDToDeathRateMapping= new HashMap();
    @SidedProxy(clientSide = "natureoverhaul.ClientProxy", serverSide = "natureoverhaul.CommonProxy")
    public static CommonProxy proxy; 
    private static String[] names=new String[]
    	{
    	"Sapling","Tree","Flower","Netherwort","Grass","Reed","Cactus","Mushroom","Mushroom Tree","Leaf"
    	};
    private static String[] optionsCategory=new String[names.length+1];
    		static{
    		for(int i=0;i<names.length;i++)
    		{
    			optionsCategory[i]=names[i]+" Options";
    		}
    		optionsCategory[names.length]="Misc Options";
    		};
    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        File cfile = event.getSuggestedConfigurationFile();
        Configuration config = new Configuration(cfile,true);
        config.load();
        for(String name:optionsCategory)
        {
        	config.addCustomCategoryComment(name,"The lower the rate, the faster the changes happen.");
        }
        for (int i=0;i<10;i++)
        {     
        	dieSets[i]=config.get(optionsCategory[i],names[i]+"Die",true).getBoolean(true);
        	growSets[i]=config.get(optionsCategory[i],names[i]+"Grow",true).getBoolean(true);
        	deathRates[i]=config.get(optionsCategory[i],names[i]+"DeathRate",1200).getInt(1200);
        	growthRates[i]=config.get(optionsCategory[i],names[i]+"GrowthRate",1200).getInt(1200);
        }
        autoSapling=config.get(optionsCategory[0],"AutoSapling",true).getBoolean(true);
        growthType=config.get(optionsCategory[0],"Growth Occurs On",3).getInt(3);
       
        lumberjack=config.get(optionsCategory[1],"Lumberjack",true).getBoolean(true);
        deathRates[9]=config.get(optionsCategory[9],names[9]+"DeathRate",2500).getInt(2500);//Leaves
        growSets[10]=config.get(optionsCategory[1],"CocoaGrows",true).getBoolean(true);//Cocoa
        growthRates[10]=config.get(optionsCategory[1],"CocoaGrowthRate",3000).getInt(3000);
        growSets[11]=config.get(optionsCategory[1],"AppleGrows",true).getBoolean(true);//Apple
        growthRates[11]=config.get(optionsCategory[1],"AppleGrowthRate",3000).getInt(3000);            
        
        defaultShroomSpread=config.get(optionsCategory[7],"default"+names[7]+"Spread",false).getBoolean(false);
        
        biomeModifiedGrowth=config.get(optionsCategory[10],"BiomeModifiedGrowth",true).getBoolean(true);
        growSets[12]=config.get(optionsCategory[10],"MossGrow",true).getBoolean(true);//Moss
        growthRates[12]=config.get(optionsCategory[10],"MossGrowthRate",2400).getInt(2400);
        //Not sure if the following can be implemented
        //reproductionRate=config.get(optionsCategory[9],"ReproductionRate",1).getInt(1);
        //wildAnimalsBreed=config.get(optionsCategory[9],"WildAnimalsBreed",true).getBoolean(true);
        //wildAnimalBreedRate=config.get(optionsCategory[9],"WildAnimalBreedRate",16000).getInt(16000);
      if (config.hasChanged())
      {        
    	  config.save();     	
      }    
    }
    @Init
    public void load(FMLInitializationEvent event)
    {	  
    	MinecraftForge.EVENT_BUS.register(this); 
    	TickRegistry.registerTickHandler(this, Side.SERVER);
    }
    @ForgeSubscribe
    public void onBoneMealUse(BonemealEvent event){
    	if (event.hasResult() && applyBonemeal(event.world, event.X, event.Y, event.Z, event.ID)){
		event.setResult(Result.ALLOW);//BoneMeal is consumed, but doesn't act vanilla
		}
    }
    @ForgeSubscribe
    public void onGrowingSapling(SaplingGrowTreeEvent event){
    	if (event.hasResult()){
    		event.setResult(Result.DENY);//Sapling doesn't grow vanilla
    	}
    }
    /**
	* Apply bonemeal to the item clicked
	* 
	* @param	id 	Item ID
	* @return	true if item is applied
	*/
	private boolean applyBonemeal(World world, int i, int j, int k, int id) {
		// Items affected; cactii, reeds, leaves, flowers and shrooms
		if(Block.blocksList[id] instanceof IGrowable) {
			((IGrowable) Block.blocksList[id]).grow(world, i, j, k);
			return true;
		} else {
			return false;
		}
	}
	public void onUpdateTick(World world, int i, int j, int k, int id)	
	{	
		if( isGrowing(id) && world.rand.nextFloat() < getGrowthProb(world, i, j, k, id)) 
		{
				System.out.println("Block "+id+" growing at "+i+","+j+","+k);
				/*	grow(world, i, j, k);
				}
				if(isMortal(id) && hasDied(world, i, j, k)) {
				death(world, i, j, k);*/
		}
	}
	/**
	* Get the growth probability
	* @param	world
	* @param	i first coordinate
	* @param	j second coordinate
	* @param	k third coordinate
	* @return	Growth probability as a float
	*/
	private float getGrowthProb(World world, int i, int j, int k, int id) {	
		float freq = getGrowthRate(id);
		if(biomeModifiedGrowth && freq>0) {
			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
			NOType type=getType(id);
			if((biome.rainfall == 0) || (biome.temperature > 1.5F)) {
				return 0.01F;
			} else if(type!=NOType.CUSTOM){
			freq *= Utils.getOptValueMult(biome.rainfall, getOptRain(id), type.getRainGrowth());
			freq *= Utils.getOptValueMult(biome.temperature, getOptTemp(id), type.getTempGrowth());
			}
		}
		if(freq>0)
			return 1F / freq;
		else
			return -1F;
	}
	/**
	* Get the death probability
	* @param	world
	* @param	i
	* @param	j
	* @param	k
	* @return	Death probability
	*/
	private float getDeathProb(World world, int i, int j, int k, int id) {			
		float freq = getDeathRate(id);
		if(biomeModifiedGrowth && freq>0) {
			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
			NOType type=getType(id);
			if((biome.rainfall == 0) || (biome.temperature > 1.5F)) {
				return 1F;
			} else if(type!=NOType.CUSTOM){
			freq *= Utils.getOptValueMult(biome.rainfall, getOptRain(id), type.getRainDeath());
			freq *= Utils.getOptValueMult(biome.temperature, getOptTemp(id), type.getTempDeath());		
			}
		}
		if(freq>0)
			return 1F / freq;
		else
			return -1F;
	}
	
	private float getOptTemp(int id) {
		return IDToOptTempMapping.get(Integer.valueOf(id));
	}
	private float getOptRain(int id) {
		return IDToOptRainMapping.get(Integer.valueOf(id));
	}
	private int getGrowthRate(int id) {
		return IDToGrowthRateMapping.get(Integer.valueOf(id));
	}
	private int getDeathRate(int id) {
		return IDToDeathRateMapping.get(Integer.valueOf(id));
	}
	private boolean isGrowing(int id){
		return IDToGrowingMapping.get(Integer.valueOf(id));	
	}
	private boolean isMortal(int id){
		return IDToDyingMapping.get(Integer.valueOf(id));
	}
	private boolean isValid(int id){
		return id>0 && id<4096 && Block.blocksList[id]!=null && Block.blocksList[id].getTickRandomly() && IDToGrowingMapping.containsKey(Integer.valueOf(id));
	}
	private NOType getType(int id){
		return IDToTypeMapping.get(Integer.valueOf(id));
	}
	/**
	* Setup sapling options
	*/
	/*private static float getSaplingGrowthRate() {
		String[] 	growthLabels = {"Both", "Decay", "Growth", "Neither"};
		int[]	growthValues = {3, 2, 1, 0};
		int[] dKeys 	= {2500, 1250, 250, 5, 10000, 5000};
		
		return 0;
		
		saps.addMappedOption("DeathRate", dKeys, labels);
		saps.addMultiOption("GrowthRate", labels);
		saps.addMappedOption("Growth Occurs On", growthValues, growthLabels);
		growthType = (Float) saps.getOption("Growth Occurs On");
		
	}*/
	
	/**
	* Setup tree options
	*//*
		int[] dKeys 	= {2500, 1250, 250, 5, 10000, 5000};
		int[] keys 	= {5, 3, 1, 0, 9, 7};
		
		tree.addMappedOption("TreeGrowthRate", keys, values);
		tree.addOption(leafDecay);
		tree.addMappedOption("DeathRate", dKeys, labels);
		
		int[] aKeys = {3000, 1200, 250, 5, 15000, 9000};
		tree.addMappedOption("CocoaGrowthRate", aKeys, labels);
		tree.addMappedOption("AppleGrowthRate", aKeys, labels);*/
	
	/**
	* Setup shroom options
	*/	/*
		int[] pKeys 	= {1200, 120, 30, 5, 15000, 4500};
		
		shroomTreeGrowth = new Float( pKeys, labels);
		shroomDeathRate  = new Float( pKeys, labels);
		
		defaultShroomSpread.setValue(false);*/
	
	/**
	* Set up misc options
	*/
		/*
		Integer[] rKeys = { 16000, 1600, 160, 16, 64000, 32000 };
		public static final Float breedRate = new Float("Wild Birth Rate", rKeys, labels);*/
	
    private static void addMapping(int id, boolean isGrowing,int growthRate, boolean isMortal,int deathRate, float optTemp, float optRain, NOType type)
    {
    	IDToGrowingMapping.put(Integer.valueOf(id), isGrowing);
    	IDToGrowthRateMapping.put(Integer.valueOf(id),growthRate);
        IDToDyingMapping.put(Integer.valueOf(id), isMortal);
        IDToDeathRateMapping.put(Integer.valueOf(id),deathRate);
        IDToOptTempMapping.put(Integer.valueOf(id), optTemp);
        IDToOptRainMapping.put(Integer.valueOf(id), optRain);
        IDToTypeMapping.put(Integer.valueOf(id), type);
    }
    @PostInit
    public void modsLoaded(FMLPostInitializationEvent event){
    	for (int i=1;i<Block.blocksList.length;i++)
    	{
    		if (Block.blocksList[i]!=null)
    			if (Block.blocksList[i] instanceof BlockOverhauled)//Priority to Blocks using the API
    			{
    				addMapping(i,true,((IGrowable)Block.blocksList[i]).getGrowthRate(),true, ((IBlockDeath)Block.blocksList[i]).getDeathRate(),-1.0F,-1.0F,NOType.CUSTOM);      			
    			}
    			else if (Block.blocksList[i] instanceof BlockGrowable || Block.blocksList[i] instanceof IGrowable)
    			{
    				addMapping(i,true,((IGrowable)Block.blocksList[i]).getGrowthRate(),false,-1,-1.0F,-1.0F,NOType.CUSTOM);
    			}
    			else if (Block.blocksList[i] instanceof BlockMortal||Block.blocksList[i] instanceof IBlockDeath)
    			{
    				addMapping(i, false, -1, true, ((IBlockDeath)Block.blocksList[i]).getDeathRate(), -1.0F, -1.0F,NOType.CUSTOM);
    			} 			
    			else if (Block.blocksList[i] instanceof BlockGrass||Block.blocksList[i] instanceof BlockTallGrass||Block.blocksList[i] instanceof BlockMycelium)
    			{
    				addMapping(i, growSets[4], growthRates[4], dieSets[4], deathRates[4], 0.7F, 0.5F,NOType.GRASS);
    			}
    			else if(Block.blocksList[i] instanceof BlockSapling)
    			{
    				addMapping(i, growSets[0], 0, dieSets[0],deathRates[0], 1.0F, 1.0F,NOType.CUSTOM);
    			}
    			else if(Block.blocksList[i] instanceof BlockLog)
    			{
    				addMapping(i,growSets[1],growthRates[1],dieSets[1],deathRates[1], 1.0F, 1.0F,NOType.LOG);
    			}
    			else if(Block.blocksList[i] instanceof BlockLeaves)
    			{
    				addMapping(i, growSets[9], growthRates[9], dieSets[9],deathRates[9], 1.0F, 1.0F,NOType.CUSTOM );
    			}
    			else if(Block.blocksList[i] instanceof BlockFlower)
    			{
    				addMapping(i,growSets[2],growthRates[2],dieSets[2],deathRates[2],0.6F,0.7F,NOType.FLOWER);
    			}
    			else if (Block.blocksList[i] instanceof BlockMushroom)
    			{
    				addMapping(i,growSets[7],growthRates[7],dieSets[7],deathRates[7],0.9F,1.0F,NOType.MUSHROOM);
    			}			
    			else if(i==Block.cobblestoneMossy.blockID)
    			{
    				addMapping(i,growSets[12],growthRates[12],false,-1,0.7F,1.0F,NOType.MOSS);			
    			}
    			else if(Block.blocksList[i] instanceof BlockCactus)
    			{
    				addMapping(i, growSets[6],growthRates[6], dieSets[6],deathRates[6], 1.5F, 0.2F,NOType.CACTUS);
    			}
    			else if(Block.blocksList[i] instanceof BlockReed)
    			{
    				addMapping(i,growSets[5],growthRates[5],dieSets[5],deathRates[5],0.8F,0.8F,NOType.REED);
    			}
    			else if(Block.blocksList[i] instanceof BlockMushroomCap)
    			{
    				addMapping(i,growSets[8],growthRates[8],dieSets[8],deathRates[8],0.9F,1.0F,NOType.MUSHROOMCAP);				
    			}	
    			else if(Block.blocksList[i] instanceof BlockNetherStalk)//In the Nether,TODO: check how biome temp and rain are set
    			{
    				addMapping(i,growSets[3],growthRates[3],dieSets[3],deathRates[3],1.0F,1.0F,NOType.NETHERSTALK);
    			}
    			else if(Block.blocksList[i] instanceof BlockCocoa)
    			{
    				addMapping(i,growSets[10],growthRates[10],false,-1,1.0F,1.0F,NOType.COCOA);
    			}		
    	}
    }
    @Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{	
		if(tickData[0] instanceof WorldServer)
		{
			world=(WorldServer) tickData[0];
			if (world.provider.dimensionId==0);
			{//In overworld
				Iterator it=world.activeChunkSet.iterator();			
				while (it.hasNext())
				{
					ChunkCoordIntPair chunkIntPair = (ChunkCoordIntPair) it.next();
					int k = chunkIntPair.chunkXPos * 16;
		            int l = chunkIntPair.chunkZPos * 16;
					Chunk chunk=null;
					if(world.getChunkProvider().chunkExists(chunkIntPair.chunkXPos,chunkIntPair.chunkZPos))
					{
						chunk=world.getChunkFromChunkCoords(chunkIntPair.chunkXPos,chunkIntPair.chunkZPos);
					}
					if (chunk!=null && chunk.isChunkLoaded && chunk.isTerrainPopulated)
					{			
						int i2;
						for (ExtendedBlockStorage blockStorage:chunk.getBlockStorageArray())
							if (blockStorage!=null && !blockStorage.isEmpty() && blockStorage.getNeedsRandomTick())
								{
								for (int j2 = 0; j2 < 3; ++j2)
			                    	{
			                        	this.updateLCG = this.updateLCG * 3 + 1013904223;
			                        	i2 = this.updateLCG >> 2;
			                        	int k2 = i2 & 15;
			                        	int l2 = i2 >> 8 & 15;
			                        	int i3 = i2 >> 16 & 15;
			                        	int j3 = blockStorage.getExtBlockID(k2, i3, l2);
			                        	Block block = Block.blocksList[j3];

			                        	if (isValid(j3))
			                        	{
			                        		onUpdateTick(world, k2 + k, i3 + blockStorage.getYLocation(), l2 + l, j3);
			                        	}
			                    	}
							/*List list= world.getPendingBlockUpdates(chunk, false);//FIXME
							if (list!=null)
							{			
								Iterator itr=list.iterator();						
								while(itr.hasNext())				
								{						
									NextTickListEntry nextTickEntry=(NextTickListEntry) itr.next();					
									if ( nextTickEntry.scheduledTime == world.getTotalWorldTime() && isValid(nextTickEntry.blockID))						
									{						
										System.out.println("first conditions checked");							
										//onUpdateTick(world,nextTickEntry.xCoord,nextTickEntry.yCoord,nextTickEntry.zCoord,nextTickEntry.blockID);						
									}						
								}	
							}*/
						}
					}
				}
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "Nature Overhaul Tick";
	}
}
