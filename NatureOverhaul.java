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
public static Boolean saplingDie=false,saplingGrow=false,treeDie=false,treeGrow=false;
public static Boolean flowerDie=false,flowerGrow=false,wortDie=false,wortGrow=false;
public static Boolean grassDie=false,grassGrow=false,reedDie=false,reedGrow=false;
public static Boolean cactiiDie=false,cactiiGrow=false,shroomDie=false,shroomGrow=false;
public static Boolean shroomTreeGrow=false,shroomTreeDie=false,cocoaGrow=false,appleGrow=false;
public static Boolean autoSapling=false,lumberjack=false,leafDecay=true;
public static Boolean defaultShroomSpread=true,biomeModifiedGrowth=true,mossGrow=true;
public static Boolean wildAnimalsBreed=true;
public static int saplingDeathRate=60000,leafDeathRate=0;
public static String saplingGrowthRate="";
public static int treeDeathRate=0,treeGrowthRate=0,flowerDeathRate=0,flowerGrowthRate=0;
public static int wortDeathRate=0,wortGrowthRate=0,grassDeathRate=0,grassGrowthRate=0;
public static int reedDeathRate=0,reedGrowthRate=0,cactiiDeathRate=0,cactiiGrowthRate=0;
public static int shroomDeathRate=0,shroomTreeGrowthRate=0,shroomGrowthRate=0,shroomTreeDeathRate=0;
public static int cocoaGrowthRate=0,appleGrowthRate=0,mossGrowthRate=0;
public static int wildAnimalBreedRate=0,reproductionRate=0;
public static int growthType=0;
private static WorldServer world;
protected int updateLCG = (new Random()).nextInt();

// Default labels
public static String[] labels = {"AVERAGE", "FAST", "SUPERFAST", "INSANE", "SUPERSLOW", "SLOW"};
public static Map stringToRateMapping = new HashMap();//TODO: Use this
static{
	stringToRateMapping.put("INSANE", 5);
	stringToRateMapping.put("SUPERFAST", 250);
	stringToRateMapping.put("FAST", 1250);
	stringToRateMapping.put("AVERAGE", 2500);
	stringToRateMapping.put("SLOW", 5000);
	stringToRateMapping.put("SUPERSLOW", 10000);
}
public static Map IDToGrowingMapping = new HashMap(),IDToDyingMapping = new HashMap();
public static Map IDToOptTempMapping = new HashMap(),IDToOptRainMapping = new HashMap();
public static Map IDToGrowthRateMapping= new HashMap(),IDToDeathRateMapping= new HashMap();
    @SidedProxy(clientSide = "natureoverhaul.ClientProxy", serverSide = "natureoverhaul.CommonProxy")
    public static CommonProxy proxy;
    private static String[] optionsCategory=new String[]//TODO: Use this more
    		{
    	"Sapling Options","Tree Options","Flower Options","Netherwort Options","Grass Options",
    	"Reed Options","Cactus Options","Mushroom Options","Misc Options"
    		};
    
    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        File cfile = event.getSuggestedConfigurationFile();
        Configuration config = new Configuration(cfile,true);
        config.load();
        for(String name:optionsCategory)
        config.addCustomCategoryComment(name,"The lower the rate, the faster the changes happen.");
        
        saplingDie=config.get("Sapling Options","SaplingDie",true).getBoolean(true);
        saplingGrow=config.get("Sapling Options","SaplingGrow",true).getBoolean(true);
        autoSapling=config.get("Sapling Options","AutoSapling",true).getBoolean(true);
        saplingDeathRate=config.get("Sapling Options","SaplingDeathRate",2500).getInt(2500);
        saplingGrowthRate=config.get("Sapling Options","SaplingGrowthRate","AVERAGE").getString(); 
        //look at labels for other values    
        growthType=config.get("Sapling Options","Growth Occurs On",3).getInt(3);
       
        treeDie=config.get("Tree Options","TreeDie",true).getBoolean(true);
        treeGrow=config.get("Tree Options","TreeGrow",true).getBoolean(true);
        lumberjack=config.get("Tree Options","Lumberjack",true).getBoolean(true);
        leafDecay=config.get("Tree Options","LeafDecay",true).getBoolean(true);
        cocoaGrow=config.get("Tree Options","CocoaGrows",true).getBoolean(true);
        appleGrow=config.get("Tree Options","AppleGrows",true).getBoolean(true);
        treeDeathRate=config.get("Tree Options","TreeDeathRate",2500).getInt(2500);
        treeGrowthRate=config.get("Tree Options","TreeGrowthRate",5).getInt(5);
        leafDeathRate=config.get("Tree Options","LeafDeathRate",2500).getInt(2500);
        cocoaGrowthRate=config.get("Tree Options","CocoaGrowthRate",3000).getInt(3000);
        appleGrowthRate=config.get("Tree Options","AppleGrowthRate",3000).getInt(3000);
       
        flowerDie=config.get("Flower Options","FlowerDie",true).getBoolean(true);
        flowerGrow=config.get("Flower Options","FlowerGrow",true).getBoolean(true);
        flowerDeathRate=config.get("Flower Options","FlowerDeathRate",1200).getInt(1200);
        flowerGrowthRate=config.get("Flower Options","FlowerGrowthRate",1200).getInt(1200);
        
        wortDie=config.get("Netherwort Options","WortDie",true).getBoolean(true);
        wortGrow=config.get("Netherwort Options","WortGrow",true).getBoolean(true);
        wortDeathRate=config.get("Netherwort Options","WortDeathRate",1200).getInt(1200);
        wortGrowthRate=config.get("Netherwort Options","WortGrowthRate",1200).getInt(1200);
       
        grassDie=config.get("Grass Options","GrassDie",true).getBoolean(true);
        grassGrow=config.get("Grass Options","GrassGrow",true).getBoolean(true);
        grassDeathRate=config.get("Grass Options","GrassDeathRate",1200).getInt(1200); 
        grassGrowthRate=config.get("Grass Options","GrassGrowthRate",1200).getInt(1200); 
        
        reedDie=config.get("Reed Options","ReedDie",true).getBoolean(true);
        reedGrow=config.get("Reed Options","ReedGrow",true).getBoolean(true);
        reedDeathRate=config.get("Reed Options","ReedDeathRate",1200).getInt(1200);
        reedGrowthRate=config.get("Reed Options","ReedGrowthRate",1200).getInt(1200);
        
        cactiiDie=config.get("Cactus Options","CactiiDie",true).getBoolean(true);
        cactiiGrow=config.get("Cactus Options","CactiiGrow",true).getBoolean(true);
        cactiiDeathRate=config.get("Cactus Options","CactiiDeathRate",1200).getInt(1200);
        cactiiGrowthRate=config.get("Cactus Options","CactiiGrowthRate",1200).getInt(1200);
       
        shroomDie=config.get("Mushroom Options","ShroomDie",true).getBoolean(true);
        defaultShroomSpread=config.get("Mushroom Options","defaultShroomSpread",false).getBoolean(false);
        shroomTreeGrow=config.get("Mushroom Options","ShroomTreeGrow",true).getBoolean(true);
        shroomTreeDie=config.get("Mushroom Options","ShroomTreeDie",true).getBoolean(true);
        shroomDeathRate=config.get("Mushroom Options","ShroomDeathRate",1200).getInt(1200);
        shroomGrow=config.get("Mushroom Options","ShroomGrow",true).getBoolean(true);
        shroomGrowthRate=config.get("Mushroom Options","ShroomGrowthRate",1200).getInt(1200);       
        shroomTreeGrowthRate=config.get("Mushroom Options","ShroomTreeGrowthRate",1200).getInt(1200);
        shroomTreeDeathRate=config.get("Mushroom Options","ShroomTreeDeathRate",1200).getInt(1200);
        biomeModifiedGrowth=config.get("Misc Options","BiomeModifiedGrowth",true).getBoolean(true);
        mossGrow=config.get("Misc Options","MossGrow",true).getBoolean(true);
        mossGrowthRate=config.get("Misc Options","MossGrowthRate",2400).getInt(2400);
        //Not sure if the following can be implemented
        reproductionRate=config.get("Misc Options","ReproductionRate",1).getInt(1);
        wildAnimalsBreed=config.get("Misc Options","WildAnimalsBreed",true).getBoolean(true);
        wildAnimalBreedRate=config.get("Misc Options","WildAnimalBreedRate",16000).getInt(16000);
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
				System.out.println("second condition checked");
				/*	grow(world, i, j, k);
				}
				if(isMortal(data[3]) && hasDied(world, i, j, k)) {
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
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		float freq = getGrowthRate(id);
		if(biomeModifiedGrowth && freq!=-1) {
			if((biome.rainfall == 0) || (biome.temperature > 1.5F)) {
				return 0.01F;
			} else {
			freq = (int) freq * BlockGrowable.getOptValueMult(biome.rainfall, getOptRain(id), 0.5F);
			freq = (int) freq * BlockGrowable.getOptValueMult(biome.temperature, getOptTemp(id), 0.5F);
			}
		}
		return 1F / freq;
	}
	private float getOptTemp(int id) {
		return (float) IDToOptTempMapping.get(Integer.valueOf(id));
	}
	private float getOptRain(int id) {
		return (float) IDToOptRainMapping.get(Integer.valueOf(id));
	}
	private int getGrowthRate(int id) {
		return (int) IDToGrowthRateMapping.get(Integer.valueOf(id));
	}
	private int getDeathRate(int id) {
		return (int) IDToDeathRateMapping.get(Integer.valueOf(id));
	}
	private boolean isGrowing(int id){
		return (boolean) IDToGrowingMapping.get(Integer.valueOf(id));	
	}
	private boolean isMortal(int id){
		return (boolean) IDToDyingMapping.get(Integer.valueOf(id));
	}
	public boolean isValid(int id){
		return id>0 && id<4096 && Block.blocksList[id]!=null && Block.blocksList[id].getTickRandomly() && IDToGrowingMapping.containsKey(Integer.valueOf(id));
	}
	/**
	* Setup sapling options
	*/
	private static float getSaplingGrowthRate() {
		String[] 	growthLabels = {"Both", "Decay", "Growth", "Neither"};
		int[]	growthValues = {3, 2, 1, 0};
		int[] dKeys 	= {2500, 1250, 250, 5, 10000, 5000};
		
		return 0;
		
		//saps.addMappedOption("DeathRate", dKeys, labels);
		//saps.addMultiOption("GrowthRate", labels);
		//saps.addMappedOption("Growth Occurs On", growthValues, growthLabels);
		//growthType = (Float) saps.getOption("Growth Occurs On");
		
	}
	
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
	
    public static void addMapping(int id, boolean isGrowing,int growthRate, boolean isMortal,int deathRate, float optTemp, float optRain)
    {
    	IDToGrowingMapping.put(Integer.valueOf(id), isGrowing);
    	IDToGrowthRateMapping.put(Integer.valueOf(id),growthRate);
        IDToDyingMapping.put(Integer.valueOf(id), isMortal);
        IDToDeathRateMapping.put(Integer.valueOf(id),deathRate);
        IDToOptTempMapping.put(Integer.valueOf(id), optTemp);
        IDToOptRainMapping.put(Integer.valueOf(id), optRain);
    }
    @PostInit
    public void modsLoaded(FMLPostInitializationEvent event){
    	for (int i=1;i<Block.blocksList.length;i++)
    	{
    		if (Block.blocksList[i]!=null)
    			if (Block.blocksList[i] instanceof BlockGrass||Block.blocksList[i] instanceof BlockTallGrass||Block.blocksList[i] instanceof BlockMycelium)
    			{
    				addMapping(i, grassGrow, grassGrowthRate, grassDie, grassDeathRate, 0.7F, 0.5F);
    			}
    			else if(Block.blocksList[i] instanceof BlockSapling)
    			{
    				addMapping(i, saplingGrow, 0, saplingDie,saplingDeathRate, 1.0F, 1.0F);
    			}
    			else if(Block.blocksList[i] instanceof BlockLog)
    			{
    				addMapping(i,treeGrow,treeGrowthRate,treeDie,treeDeathRate, 1.0F, 1.0F);
    			}
    			else if(Block.blocksList[i] instanceof BlockLeaves)
    			{
    				addMapping(i, false, 0, leafDecay,leafDeathRate, 1.0F, 1.0F );
    			}
    			else if(Block.blocksList[i] instanceof BlockFlower)
    			{
    				addMapping(i,flowerGrow,flowerGrowthRate,flowerDie,flowerDeathRate,0.6F,0.7F);
    			}
    			else if (Block.blocksList[i] instanceof BlockMushroom)
    			{
    				addMapping(i,shroomGrow,shroomGrowthRate,shroomDie,shroomDeathRate,0.9F,1.0F);
    			}			
    			else if(i==Block.cobblestoneMossy.blockID)
    			{
    				addMapping(i,mossGrow,mossGrowthRate,false,0,1.0F,1.0F);			
    			}
    			else if(Block.blocksList[i] instanceof BlockCactus)
    			{
    				addMapping(i, cactiiGrow,cactiiGrowthRate, cactiiDie,cactiiDeathRate, 1.5F, 0.2F);
    			}
    			else if(Block.blocksList[i] instanceof BlockReed)
    			{
    				addMapping(i,reedGrow,reedGrowthRate,reedDie,reedDeathRate,0.8F,0.8F);
    			}
    			else if(Block.blocksList[i] instanceof BlockMushroomCap)
    				{
    				addMapping(i,shroomTreeGrow,shroomTreeGrowthRate,shroomTreeDie,shroomTreeDeathRate,1.0F,1.0F);				
    				}	
    			else if(Block.blocksList[i] instanceof BlockNetherStalk)
    			{
    				addMapping(i,wortGrow,wortGrowthRate,wortDie,wortDeathRate,1.0F,1.0F);
    			}
    			else if(Block.blocksList[i] instanceof BlockCocoa)
    			{
    				addMapping(i,cocoaGrow,cocoaGrowthRate,false,0,1.0F,1.0F);
    			}		
    	}
    }
    @Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{	
		if(tickData[0] instanceof WorldServer)
		{
			world=(WorldServer) tickData[0];
			if (world.provider.dimensionId==0 /*&& world.getWorldInfo().getWorldTime()%5==0*/);
			{//In overworld, every 5 tick
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
							/*List list= world.getPendingBlockUpdates(chunk, false);FIXME
							if (list!=null)
							{			
								Iterator itr=list.iterator();						
								while(itr.hasNext())				
								{						
									NextTickListEntry nextTickEntry=(NextTickListEntry) itr.next();					
									if ( nextTickEntry.scheduledTime == world.getWorldInfo().getWorldTotalTime() && isValid(nextTickEntry.blockID))						
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
