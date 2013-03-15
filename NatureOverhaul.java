package natureoverhaul;
//Author: Clinton Alexander
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;

//@Mod(modid = "NatureOverhaul", name = "Nature Overhaul", version = "0.0.1")
//@NetworkMod(clientSideRequired = false, serverSideRequired = false,
//clientPacketHandlerSpec = @SidedPacketHandler(channels = { "NatureOverhaul" }, packetHandler = ClientPacketHandler.class),
//serverPacketHandlerSpec = @SidedPacketHandler(channels = { "NatureOverhaul" }, packetHandler = ServerPacketHandler.class))
public class NatureOverhaul extends DummyModContainer
{	@Instance ("NatureOverhaul")
	public static NatureOverhaul instance;
	
    @SidedProxy(clientSide = "natureoverhaul.ClientProxy", serverSide = "natureoverhaul.CommonProxy")
    public static CommonProxy proxy;
    @Override
    public String getModId()
    {
        return "NatureOverhaul";
    }
    @Override
    public String getName()
    {
        return "Nature Overhaul";
    }
    @Override
    public String getVersion()
    {
        return "0.0.1";
    }
    @Override
    public boolean isNetworkMod()
    {
        return true;
    }
    @Override
    public String getDisplayVersion()
    {
        return getVersion();
    }
    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        File cfile = event.getSuggestedConfigurationFile();
        Configuration config = new Configuration(cfile,true);
        config.load();
        
        //config.addCustomCategoryComment("","The lower the rate, the faster the changes happen.");
        
        final Boolean saplingDie=config.get("Sapling Options","SaplingDie",true).getBoolean(true);
        final Boolean saplingGrow=config.get("Sapling Options","SaplingGrow",true).getBoolean(true);
        final Boolean autoSapling=config.get("Sapling Options","AutoSapling",true).getBoolean(true);
        final int saplingDeathRate=config.get("Sapling Options","SaplingDeathRate",2500).getInt(2500);
        final String saplingGrowthRate=config.get("Sapling Options","SaplingGrowthRate","AVERAGE").value; 
        //look at labels for other values    
        final int growthType=config.get("Sapling Options","Growth Occurs On",3).getInt(3);
       
        final Boolean treeDie=config.get("Tree Options","TreeDie",true).getBoolean(true);
        final Boolean treeGrow=config.get("Tree Options","TreeGrow",true).getBoolean(true);
        final Boolean lumberjack=config.get("Tree Options","Lumberjack",true).getBoolean(true);
        final Boolean leafDecay=config.get("Tree Options","LeafDecay",true).getBoolean(true);
        final Boolean cocoaGrows=config.get("Tree Options","CocoaGrows",true).getBoolean(true);
        final Boolean appleGrows=config.get("Tree Options","AppleGrows",true).getBoolean(true);
        final int treeDeathRate=config.get("Tree Options","TreeDeathRate",2500).getInt(2500);
        final int treeGrowthRate=config.get("Tree Options","TreeGrowthRate",5).getInt(5);
        final int cocoaGrowthRate=config.get("Tree Options","CocoaGrowthRate",3000).getInt(3000);
        final int appleGrowthRate=config.get("Tree Options","AppleGrowthRate",3000).getInt(3000);
       
        final Boolean flowerDie=config.get("Flower Options","FlowerDie",true).getBoolean(true);
        final Boolean flowerGrow=config.get("Flower Options","FlowerGrow",true).getBoolean(true);
        final int flowerDeathRate=config.get("Flower Options","FlowerDeathRate",1200).getInt(1200);
        final int flowerGrowthRate=config.get("Flower Options","FlowerGrowthRate",1200).getInt(1200);
        
        final Boolean wortDie=config.get("Netherwort Options","WortDie",true).getBoolean(true);
        final Boolean wortGrow=config.get("Netherwort Options","WortGrow",true).getBoolean(true);
        final int wortDeathRate=config.get("Netherwort Options","WortDeathRate",1200).getInt(1200);
        final int wortGrowthRate=config.get("Netherwort Options","WortGrowthRate",1200).getInt(1200);
       
        final Boolean grassDie=config.get("Grass Options","GrassDie",true).getBoolean(true);
        final Boolean grassGrow=config.get("Grass Options","GrassGrow",true).getBoolean(true);
        final int grassDeathRate=config.get("Grass Options","GrassDeathRate",1200).getInt(1200); 
        final int grassGrowthRate=config.get("Grass Options","GrassGrowthRate",1200).getInt(1200); 
        
        final Boolean reedDie=config.get("Reed Options","ReedDie",true).getBoolean(true);
        final Boolean reedGrow=config.get("Reed Options","ReedGrow",true).getBoolean(true);
        final int reedDeathRate=config.get("Reed Options","ReedDeathRate",1200).getInt(1200);
        final int reedGrowthRate=config.get("Reed Options","ReedGrowthRate",1200).getInt(1200);
        
        final Boolean cactiiDie=config.get("Cactus Options","CactiiDie",true).getBoolean(true);
        final Boolean cactiiGrow=config.get("Cactus Options","CactiiGrow",true).getBoolean(true);
        final int cactiiDeathRate=config.get("Cactus Options","CactiiDeathRate",1200).getInt(1200);
        final int cactiiGrowthRate=config.get("Cactus Options","CactiiGrowthRate",1200).getInt(1200);
       
        final Boolean shroomDie=config.get("Mushroom Options","ShroomDie",true).getBoolean(true);
        final Boolean defaultShroomSpread=config.get("Mushroom Options","defaultShroomSpread",false).getBoolean(false);
        final Boolean shroomTreeGrow=config.get("Mushroom Options","ShroomTreeGrow",true).getBoolean(true);
        final int shroomDeathRate=config.get("Mushroom Options","ShroomDeathRate",1200).getInt(1200);
        final Boolean shroomGrow=config.get("Mushroom Options","ShroomGrow",true).getBoolean(true);
        final int shroomGrowthRate=config.get("Mushroom Options","ShroomGrowthRate",1200).getInt(1200);       
        final int shroomTreeGrowthRate=config.get("Mushroom Options","ShroomTreeGrowthRate",1200).getInt(1200);
        //Water fix changes BlockFlowing ,unnecessary ?
        final Boolean biomeModifiedGrowth=config.get("Misc Options","BiomeModifiedGrowth",true).getBoolean(true);
        final Boolean mossGrow=config.get("Misc Options","MossGrow",true).getBoolean(true);
        final int mossGrowthRate=config.get("Misc Options","MossGrowthRate",2400).getInt(2400);
        //final Boolean infiniteFire=config.get("Misc Options","InfiniteFire",true).getBoolean(true);
        final Boolean waterFix=config.get("Misc Options","WaterFix",true).getBoolean(true);
        final int reproductionRate=config.get("Misc Options","ReproductionRate",1).getInt(1);
        final Boolean wildAnimalsBreed=config.get("Misc Options","WildAnimalsBreed",true).getBoolean(true);
        final int wildAnimalBreedRate=config.get("Misc Options","WildAnimalBreedRate",16000).getInt(16000);
        
        config.save();
        //proxy.preInit(cfile);
    }
    @Init
    public void load(FMLInitializationEvent event)
    {	  MinecraftForge.EVENT_BUS.register(this);  	          
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
	public void onUpdateTick(World world, int x, int y, int z, Random random)	{
		System.out.println("tick done");
		if(!world.isRemote) {
			int id=world.getBlockId(x,y,z);
			String name=Block.blocksList[id].getBlockName();			
			if(isGrowing(name) && Math.random()<getGrowthProb(world, x, y, z)) {
			/*	grow(world, i, j, k);
			}
			if(isMortal(name) && hasDied(world, i, j, k)) {
				death(world, i, j, k);*/
			}
		}	
	}
	/*
	public static Map IDToGrowingMapping = new HashMap();
    public static Map IDToDyingMapping = new HashMap();
	
    public static void addMapping(int id, boolean isGrowing, boolean isMortal)
    {
    	IDToGrowingMapping.put(Integer.valueOf(id), isGrowing);
        IDToDyingMapping.put(Integer.valueOf(id), isMortal);     
    }*/

	private boolean isMortal(String name) {
		if(name.contains("sapling"))
			return saplingDie;
		else if(name.contains("tallGrass"))
			return grassDie;
		else if(name.contains("flower")||name.contains("rose"))
			return flowerDie;
		else if(name.contains("mushroom"))
			return shroomDie;
		else if(name.contains("cactus"))
			return cactiiDie;
		else if(name.contains("reed"))
			return reedDie;
		/*else if(name.contains("stem"))
			return stemDie;*/
		else if(name.contains("netherstalk"))
			return wortDie;
		
		return false;
	}
	private boolean isGrowing(String name) {
		if(name.contains("sapling"))
			return saplingGrow;
		else if(name.contains("tallgrass"))
			return grassGrow;
		else if(name.contains("flower")||name.contains("rose"))
			return flowerGrow;
		else if(name.contains("mushroom"))
			return shroomGrow;
		else if(name.contains("cactus"))
			return cactiiGrow;
		else if(name.contains("reed"))
			return reedGrow;
		/*else if(name.contains("Stem"))
			return stemGrow;*/
		else if(name.contains("netherstalk"))
			return wortGrow;
		
		return false;
		
	}
	/**
	* Get the growth probability
	* @param	world
	* @param	i
	* @param	j
	* @param	k
	* @return	Growth probability
	*/
	public float getGrowthProb(World world, int i, int j, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
		int id=world.getBlockId(i,j,k);
		String name=Block.blocksList[id].getBlockName();
		float freq = getGrowthRate(name);
		if(NatureOverhaul.biomeModifiedGrowth && freq!=-1) {
			if((biome.rainfall == 0) || (biome.temperature > 1.5F)) {
				return 0.01F;
			} else {
			freq = (int) freq * BlockGrowable.getOptValueMult(biome.rainfall, optRain(name), 0.5F);
			freq = (int) freq * BlockGrowable.getOptValueMult(biome.temperature, optTemp(name), 0.5F);
			}
		}
		return 1F / freq;
	}
	private float optTemp(String name) {
		if(name.contains("tallgrass"))
			return 0.7F;
		else if(name.contains("flower")||name.contains("rose"))
			return 0.6F;
		else if(name.contains("mushroom"))
			return 0.9F;
		else if(name.contains("cactus"))
			return 1.5F;
		else if(name.contains("reed"))
			return 0.8F;
		/*else if(name.contains("Stem"))
			return xF;*/		
		return -1.0F;
	}
	private float optRain(String name) {
		if(name.contains("tallgrass"))
			return 0.5F;
		else if(name.contains("flower")||name.contains("rose"))
			return 0.7F;
		else if(name.contains("mushroom"))
			return 1.0F;
		else if(name.contains("cactus"))
			return 0.2F;
		else if(name.contains("reed"))
			return 0.8F;
		/*else if(name.contains("Stem"))
			return xF;*/
		return -1.0F;
	}
	private float getGrowthRate(String name) {
		if(name.contains("sapling"))
			return getSaplingGrowthRate();
		else if(name.contains("tallgrass"))
			return grassGrowthRate;
		else if(name.contains("flower"))
			return flowerGrowthRate;
		else if(name.contains("mushroom"))
			return shroomGrowthRate;
		else if(name.contains("cactus"))
			return cactiiGrowthRate;
		else if(name.contains("reed"))
			return reedGrowthRate;
		/*else if(name.contains("Stem"))
			return stemGrowthRate;*/
		else if(name.contains("netherstalk"))
			return wortGrowthRate;
		
		return -1;
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
	*/
	private static void setupTreeOptions() {
		int[] dKeys 	= {2500, 1250, 250, 5, 10000, 5000};
		int[] keys 	= {5, 3, 1, 0, 9, 7};
		String[]  values 	= {"DEFAULT/AVERAGE", "FAST", "VERY FAST", "INSTANT", "VERY SLOW", "SLOW"};
		
		//tree.addMappedOption("TreeGrowthRate", keys, values);
		//tree.addOption(leafDecay);
		//tree.addMappedOption("DeathRate", dKeys, labels);
		
		// Tree droptions
		int[] aKeys = {3000, 1200, 250, 5, 15000, 9000};
		//tree.addMappedOption("CocoaGrowthRate", aKeys, labels);
		//tree.addMappedOption("AppleGrowthRate", aKeys, labels);
		
	}
	
	/**
	* Setup shroom options
	*/
	private static void setupShroomOptions() {	
		int[] pKeys 	= {1200, 120, 30, 5, 15000, 4500};
		/*
		shroomTreeGrowth = new Float( pKeys, labels);
		shroomDeathRate  = new Float( pKeys, labels);
		
		shrooms.addMappedOption("ShroomGrowthRate", pKeys, labels);
		
		defaultShroomSpread.setValue(false);*/
	}
	
	/**
	* Set up flower options
	*/
	private static void addFlowers() {	
		int[] pKeys 	= {1200, 120, 30, 5, 15000, 4500};
		/*		
		Float FlowerDeathRate = new Float( pKeys, labels);
		Float FlowerGrowthRate = new Float( pKeys, labels);
				
		WortDeathRate = new Float( pKeys, labels);
		WortGrowthRate = new Float( pKeys, labels);
				
		CactiiDeathRate = new Float( pKeys, labels);
		CactiiGrowthRate = new Float( pKeys, labels);
		
		ReedDeathRate = new Float( pKeys, labels);
		ReedGrowthRate = new Float( pKeys, labels);
				
		GrassDeathRate = new Float( pKeys, labels);
		GrassGrowthRate = new Float( pKeys, labels);	*/
	}
	
	/**
	* Set up misc options
	*/
	private static void addMiscOptions() {
		/*
		int[] pKeys 	= {2400, 240, 30, 5, 30000, 9000};
		Float ("MossGrowthRate", pKeys, labels);
		Integer[] rKeys = { 16000, 1600, 160, 16, 64000, 32000 };
		public static final Float breedRate = new Float("Wild Birth Rate", rKeys, labels);*/
	}
	public static final Boolean saplingDie=false,saplingGrow=false;
	public static final Boolean treeDie=false,treeGrow=false;
	public static final Boolean flowerDie=false,flowerGrow=false;
	public static final Boolean  wortDie=false,wortGrow=false;
	public static final Boolean grassDie=false,grassGrow=false;
	public static final Boolean reedDie=false,reedGrow=false;
	public static final Boolean cactiiDie=false,cactiiGrow=false;
	public static final Boolean shroomDie=false,shroomGrow=false;
	public static final Boolean shroomTreeGrow=false;
	public static final Boolean autoSapling=false,lumberjack=false;
	public static final Boolean cocoaGrows=false,appleGrows=false;
	public static final Boolean defaultShroomSpread=true;
	public static final Boolean biomeModifiedGrowth=true;
	public static final Boolean mossGrow=true;
	public static final Boolean waterFix=true;
	public static final Boolean wildAnimalsBreed=true;
	public static final Boolean leafDecay=true;
	public static final int saplingDeathRate=60000;
	public static final String saplingGrowthRate="";
	public static final int treeDeathRate=0,treeGrowthRate=0;
	public static final int flowerDeathRate=0,flowerGrowthRate=0;
	public static final int  wortDeathRate=0,wortGrowthRate=0;
	public static final int grassDeathRate=0,grassGrowthRate=0;
	public static final int reedDeathRate=0,reedGrowthRate=0;
	public static final int cactiiDeathRate=0,cactiiGrowthRate=0;
	public static final int shroomDeathRate=0,shroomTreeGrowthRate=0;
	public static final int shroomGrowthRate=0;
	public static final int cocoaGrowthRate=0,appleGrowthRate=0;
	public static final int mossGrowthRate=0;
	public static final int wildAnimalBreedRate=0,reproductionRate=0;
	public static final int growthType=0;
	
	/*public static World tickedWorld;
	public static int tickX,tickY,tickZ;
	public static Random tickRand;*/
	
	// Default labels
	public static String[] labels = {"AVERAGE", "FAST", "SUPERFAST", "INSANE", "SUPERSLOW", "SLOW"};
}
