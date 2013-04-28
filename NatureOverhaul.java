package mods.natureoverhaul;
//Author: Clinton Alexander
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
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
import net.minecraft.block.BlockStem;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "NatureOverhaul", name = "Nature Overhaul", version = "0.1")
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class NatureOverhaul implements ITickHandler{
	@Instance ("NatureOverhaul")
	public static NatureOverhaul instance;
	public static Boolean autoSapling=false,lumberjack=false,moddedBonemeal=true,
			killLeaves=true,biomeModifiedRate=true,useStarvingSystem=true,
			mossCorruptStone=true,customDimension=true,wildAnimalsBreed=true;
	public static int wildAnimalBreedRate=0,growthType=0;
	protected int updateLCG = (new Random()).nextInt();
	
	private static final int[] DEFAULT_ID_MAP={17,18};
	private static HashMap<String,Integer> valueToGrowthTypeMapping = new HashMap();
	static{
		valueToGrowthTypeMapping.put("Neither", 0);
		valueToGrowthTypeMapping.put("Growth", 1);
		valueToGrowthTypeMapping.put("Decay", 2);
		valueToGrowthTypeMapping.put("Both", 3);
	}
	private static HashMap<Integer,NOType> IDToTypeMapping = new HashMap();
	private static HashMap<Integer,Boolean> IDToGrowingMapping = new HashMap(),IDToDyingMapping = new HashMap();
	private static HashMap<Integer,Float> IDToOptTempMapping = new HashMap(),IDToOptRainMapping = new HashMap();
	private static HashMap<Integer,Integer> IDToGrowthRateMapping= new HashMap(),IDToDeathRateMapping= new HashMap();
    private static HashMap<Integer,Integer> LogToLeafMapping=new HashMap();
	private static String[] names=new String[]
    	{
    	"Sapling","Tree","Plants","Netherwort","Grass","Reed","Cactus","Mushroom","Mushroom Tree","Leaf","Crops","Moss","Cocoa"
    	};
    public static boolean[] dieSets=new boolean[names.length],growSets=new boolean[names.length+1];
	public static int[] deathRates=new int[names.length],growthRates=new int[names.length+1];
    private static String[] optionsCategory=new String[names.length+1];
    		static{
    		for(int i=0;i<names.length;i++)
    		{
    			optionsCategory[i]=names[i]+" Options";
    		}
    		optionsCategory[names.length]="Misc Options";
    		};
    public static int[] idMapForTree=DEFAULT_ID_MAP;
    
    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        Configuration config = new Configuration(event.getSuggestedConfigurationFile(),true);
        config.load();
        for(String name:optionsCategory)
        {
        	config.addCustomCategoryComment(name,"The lower the rate, the faster the changes happen.");
        }
    	config.addCustomCategoryComment(optionsCategory[2],"Plants are flower, deadbush, lilypad and tallgrass");
    	
    	autoSapling=config.get(optionsCategory[0],"AutoSapling",true).getBoolean(true);
        for (int i=0;i<names.length;i++)
        {     
        	dieSets[i]=config.get(optionsCategory[i],names[i]+" Die",true).getBoolean(true);
        	growSets[i]=config.get(optionsCategory[i],names[i]+" Grow",true).getBoolean(true);
        	deathRates[i]=config.get(optionsCategory[i],names[i]+" Death Rate",1200).getInt(1200);
        	growthRates[i]=config.get(optionsCategory[i],names[i]+" Growth Rate",1200).getInt(1200);
        }
        //Toggle between alternative time of growth for sapling
        growthType=valueToGrowthTypeMapping.get(config.get(optionsCategory[0],"Sapling grow on","Both").getString());
        //Toggle for lumberjack system on trees
        lumberjack=config.get(optionsCategory[1],"Lumberjack",true).getBoolean(true);
        //Apples don't have a dying system, because it is only an item
        growSets[names.length]=config.get(optionsCategory[9],"Apple Grows",true).getBoolean(true);
        growthRates[names.length]=config.get(optionsCategory[9],"Apple Growth Rate",3000).getInt(3000);            
        //Force remove leaves after killing a tree, instead of letting Minecraft doing it
        killLeaves=config.get(optionsCategory[9],"Enable leaves decay on tree death",true).getBoolean(true);      
        //Toggle so Stone can turn into Mossy Cobblestone
        mossCorruptStone=config.get(optionsCategory[11],"Enable moss growing on stone",true).getBoolean(true);      
        //Misc options
        useStarvingSystem=config.get(optionsCategory[names.length],"Enable starving system",true).getBoolean(true);      
        biomeModifiedRate=config.get(optionsCategory[names.length],"Enable biome specific rates",true).getBoolean(true);
        moddedBonemeal=config.get(optionsCategory[names.length],"Enable modded Bonemeal",true).getBoolean(true);
        customDimension=config.get(optionsCategory[names.length],"Enable custom dimensions",true).getBoolean(true);
        idMapForTree=config.get(optionsCategory[names.length],"Log ids linked to Leaves",DEFAULT_ID_MAP).getIntList(); 
        wildAnimalsBreed=config.get(optionsCategory[names.length],"Enable wild animals Breed",true).getBoolean(true);
        wildAnimalBreedRate=config.get(optionsCategory[names.length],"Wild animals breed rate",16000).getInt(16000);
      if (config.hasChanged())
      {        
    	  config.save();     	
      }    
    }
    @Init
    public void load(FMLInitializationEvent event)
    {	  
    	MinecraftForge.EVENT_BUS.register(this); 
    	//TickRegistry.registerTickHandler(this, Side.CLIENT);
    	TickRegistry.registerTickHandler(this, Side.SERVER);
    }
    @ForgeSubscribe//Event for Wild Breeding
    public void onLivingUpdateEvent(LivingUpdateEvent event){
    	if(wildAnimalsBreed && event.entityLiving instanceof EntityAnimal)
    	{
    		EntityAnimal ent =(EntityAnimal)event.entityLiving;
    		if(!ent.worldObj.isRemote && !ent.isChild() && ent.inLove == 0 /*&& ent.breeding == 0*/) {
    			if(ent.worldObj.rand.nextInt(wildAnimalBreedRate) == 0) {
    				ent.inLove = 600;
    			}
    		}
    	}
    }
    @ForgeSubscribe//Event for Lumberjack system
    public void onPlayerInteracting(PlayerInteractEvent event){
    	if(lumberjack && event.action==Action.LEFT_CLICK_BLOCK){
    		ItemStack itemstack = event.entityPlayer.getCurrentEquippedItem();
    		if(itemstack != null) {
    			Item it = itemstack.getItem();
    			//Check for an axe in player hand
    			if(it!=null && it instanceof ItemAxe){
    				int id=event.entityPlayer.worldObj.getBlockId(event.x, event.y, event.z);
    				//Check for a registered log block
    				if(isValid(id) ){
    					World world=event.entityPlayer.worldObj;
    					int i=event.x;
    					int j=event.y;
    					int k=event.z;
    					NOType type=IDToTypeMapping.get(Integer.valueOf(id));
    					if(type==NOType.LOG && TreeUtils.isTree(world, i, j, k, type, true))
    					{// Damage item compared to the number of blocks found
					  		int damage = TreeUtils.killTree(world, i, j, k, id, false);
        					itemstack.damageItem(damage - 1, event.entityPlayer);
        					if(itemstack.stackSize <= 0) 
        						event.entityPlayer.destroyCurrentEquippedItem();
    					}
    				}
    			}
    		}
    	}	
    }
    @ForgeSubscribe//Event for AutoSapling
    public void onSaplingItemDead(ItemExpireEvent event){	
    	if(autoSapling && growthType%2!=0)
    	{
    		EntityItem ent=event.entityItem;
    		if(ent.motionX<0.001 && ent.motionZ<0.001){
    			ItemStack item = ent.getEntityItem();
    			if(isValid(item.itemID) && IDToTypeMapping.get(Integer.valueOf(item.itemID))==NOType.SAPLING){
    				ent.worldObj.setBlock(MathHelper.floor_double(ent.posX), MathHelper.floor_double(ent.posY), MathHelper.floor_double(ent.posZ), item.itemID, item.getItemDamage(), 3);
    				//System.out.println("AutoSapling system activated");
    			}
    		}
    	}
    }
    @ForgeSubscribe//Event for modded Bonemeal
    public void onBoneMealUse(BonemealEvent event){
    	if (moddedBonemeal && event.hasResult())
    	{
    		if( applyBonemeal(event.world, event.X, event.Y, event.Z, event.ID)){
    			event.setResult(Result.ALLOW);//BoneMeal is consumed, but doesn't act vanilla
    		}
    		else
    			event.setResult(Result.DEFAULT);
    	}
    }
    @ForgeSubscribe//Event for Sapling growthType
    public void onGrowingSapling(SaplingGrowTreeEvent event){
    	if ( growthType%2==0 && event.hasResult()){
    		event.setResult(Result.DENY);//Sapling doesn't grow vanilla with even growType
    	}
    }
    /**
	* Apply bonemeal to the location clicked
	* 
	* @param	id 	block ID
	* @return	true if item is applied
	*/
	private boolean applyBonemeal(World world, int i, int j, int k, int id) {
		if (isValid(id) && isGrowing(id)){
			NOType type = IDToTypeMapping.get(Integer.valueOf(id));
			if (type!=NOType.GRASS)
			{
				grow(world, i, j, k, id, type);
				return true;
			}
			return false;
		}
		else {
			return false;
		}
	}
	/**
	* Called from the world tick with a valid id
	* call checks for grow and die booleans, and probabilities
	* then call grow or death general methods
	*/
	private void onUpdateTick(World world, int i, int j, int k, int id)	
	{	
		NOType type=Utils.getType(id);
		if( isGrowing(id) && world.rand.nextFloat() < getGrowthProb(world, i, j, k, id, type)) 
		{
			//System.out.println("Block "+id+" growing at "+i+","+j+","+k);
			grow(world, i, j, k, id, type);				
		}
		if (isMortal(id) && (hasDied(world, i, j, k, id, type) || world.rand.nextFloat() < getDeathProb(world, i, j, k, id, type)))
		{
			//System.out.println("Block "+id+" dying at "+i+","+j+","+k);		
			death(world, i, j, k, id, type);
		}
	}
	/**
	* Check whether this block has died on this tick for any reason
	*
	* @return	True if plant has died
	*/
	private boolean hasDied(World world, int i, int j, int k, int id, NOType type) {
		switch(type){
		case CUSTOM:
			if (Block.blocksList[id] instanceof IBlockDeath)
				return ((IBlockDeath)Block.blocksList[id]).hasDied(world,i,j,k);
			return false;
		case CACTUS:case REED:case GRASS:case PLANT:case MUSHROOM:case COCOA:
			case FERTILIZED:case MOSS:case NETHERSTALK:case SAPLING:
				return useStarvingSystem && hasStarved(world, i, j, k, type);
		case LOG:case MUSHROOMCAP:case LEAVES://Trees and leaves are only randomly dying
			return false;	
		default:
			return false;	
		}	
	}
	/**
	* Checks whether this block has starved on this tick
	* by being surrounded by too many of it's kind
	*
	* @return	True if plant has starved
	*/
	private boolean hasStarved(World world, int i, int j, int k, NOType type) {
		int radius = 1;
		int maxNeighbours = 9;
		int foundNeighbours = 0;
		switch(type){
		case CACTUS:case REED:
			radius = 2;
			break;
		case MOSS:
			maxNeighbours = 15;
			break;
		case NETHERSTALK:case COCOA:case GRASS:case MUSHROOM:case PLANT:
			maxNeighbours = 5;
			break;
		case FERTILIZED:
			maxNeighbours = 6;
			break;
		case SAPLING:
			radius = 2;
			maxNeighbours = 5;
			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);		
			if(biome.temperature > 1F) {
				radius = 4;
				maxNeighbours = 1;
			}		
			if(biome.rainfall < 0.5F) {
				radius++;
			}					
			if(biome.temperature <= 0F) 
				maxNeighbours = 1;			
			maxNeighbours = maxNeighbours + (int) Math.ceil(biome.rainfall / 0.2) - 2;		
			if(maxNeighbours < 0) 
				maxNeighbours=0;		
			break;
		default://All cases have been set, this is for safety
			return false;
		}	
		if((radius > 0) && (radius < 10)) {
			for(int x = i - radius; x < i + radius; x++) {
				for(int y = j - radius; y < j + radius; y++) {
					for(int z = k - radius; z < k + radius; z++) {
						if((i != x) || (j != y) || (k != z)) {
							int blockID = world.getBlockId(x, y, z);
							if(foundNeighbours <= maxNeighbours && isValid(blockID) && Utils.getType(blockID) == type) {
								foundNeighbours++;
							}
						}
					}
				}
			}
		}	
		return (foundNeighbours > maxNeighbours);
	}
	private void death(World world, int i, int j, int k, int id, NOType type) {
		switch(type){
		case CUSTOM:
			if (Block.blocksList[id] instanceof IBlockDeath)
				((IBlockDeath)Block.blocksList[id]).death(world, i, j, k);
			return;
		case CACTUS:case REED://Disappear completely from top to bottom
			int y = j;
			// Get to the top so to avoid any being dropped since this is death
			while(world.getBlockId(i, y + 1, k) == id) {
				y = y + 1;
			}
			// Scan back down and delete
			while(world.getBlockId(i, y, k) == id) {
				world.setBlockToAir(i, y, k);
				y--;
			}
			return;		
		case LOG:case MUSHROOMCAP:
			if(TreeUtils.isTree(world, i, j, k, type, false))
			{
				TreeUtils.killTree(world, i, Utils.getLowestTypeJ(world, i, j, k , type), k, id, type==NOType.LOG?killLeaves:false);
			}
			return;
		case MOSS://Return to cobblestone
			world.setBlock(i,j,k,Block.cobblestone.blockID);
			return;
		case LEAVES://Has a chance to emit a sapling if sets accordingly
			if(growthType > 1 && world.rand.nextFloat() < getGrowthProb(world, i, j, k, Block.sapling.blockID, NOType.SAPLING))
				Utils.emitItem(world, i, j, k, new ItemStack(Block.sapling, 1, 
						 world.getBlockMetadata(i, j, k) % 4));
			world.setBlockToAir(i, j, k);//Then disappear
			return;
		case NETHERSTALK:case PLANT:case MUSHROOM:case COCOA:case SAPLING:
			world.setBlockToAir(i, j, k);//Disappear
			return;
		case GRASS: //Return to dirt
			world.setBlock(i,j,k,Block.dirt.blockID);
			return;
		case FERTILIZED: //Ungrow, or turn to dirt if too low
			int meta = world.getBlockMetadata(i, j, k);
			if (meta>=1)
				world.setBlockMetadataWithNotify(i, j, k, meta-1, 2);
			else {
				world.setBlockToAir(i,j,k);
				world.setBlock(i,j-1,k,Block.dirt.blockID);
			}	
			return;
		default:
			return;		
		}
	}
	
	private void grow(World world, int i, int j, int k, int id, NOType type) {
		int scanSize;
		switch(type){
		case CUSTOM:
			if (Block.blocksList[id] instanceof IGrowable)
				((IGrowable)Block.blocksList[id]).grow(world, i, j, k);
			return;
		case CACTUS:case REED://Grow on top if too low, or on a neighbor spot		
			if(TreeUtils.getTreeHeight(world, i, j, k, id)>2)
			{//Find a neighbor spot for new one
				scanSize = 2;
				for(int x = i - scanSize; x <= i + scanSize; x++) {
					for(int y = j - scanSize; y <= j + scanSize; y++) {
						for(int z = k - scanSize; z <= k + scanSize; z++) {
							if(Block.blocksList[id].canPlaceBlockAt(world, x, y+1, z)){
								world.setBlock(x, y+1, z, id);
								return;
							}
						}	
					}		
				}
			}
			else{
				int height = j;
				// Get to the top
				while(world.getBlockId(i, height + 1, k) == id) {
					height = height + 1;
				}
				if (world.getBlockId(i, height + 1, k) == 0)
					world.setBlock(i, height + 1, k, id);//Grow on top
			}				
			return;
		case COCOA://Emit cocoa dye
			if(world.getBlockId(i, j - 1, k) == 0 && cocoaCanGrow(world,i,k)) {
				Utils.emitItem(world, i, j - 1, k, new ItemStack(Item.dyePowder, 1, 3));			
			}
			return;
		case LEAVES://Emit apples and saplings (with odd growthType)
			if (world.getBlockId(i, j - 1, k) == 0 && appleCanGrow(world, i, k) 
			&& Math.random()<(double)getGrowthProb( world, i, j, k, id, NOType.APPLE))
				Utils.emitItem(world, i, j - 1, k, new ItemStack(Item.appleRed));
			if(growthType % 2 ==1 && world.getBlockId(i, j + 1, k) == 0) 
				Utils.emitItem(world, i, j + 1, k, new ItemStack(Block.sapling, 1,
										world.getBlockMetadata(i, j, k) % 4));
			return;
		case SAPLING://Use sapling vanilla method for growing a tree
			((BlockSapling) Block.blocksList[id]).growTree(world, i, j, k, world.rand);
			return;
		case LOG:case MUSHROOMCAP:
			if (TreeUtils.isTree(world, i, j, k, type, false))
			{		
				TreeUtils.growTree(world, i, j, k, id, type);
			}		
			return;
		case GRASS://Replace surrounding dirt with grass
			scanSize = 1;
			for(int x = i - scanSize; x <= i + scanSize; x++) {
				for(int y = j - scanSize; y <= j + scanSize; y++) {
					for(int z = k - scanSize; z <= k + scanSize; z++) {
						if(world.getBlockId(x, y, z) == Block.dirt.blockID){
							world.setBlock(x, y, z, id);
						}
					}
				}
			}
			return;
		case PLANT:case NETHERSTALK:
			scanSize = 2;
			for(int x = i - scanSize; x <= i + scanSize; x++) {
				for(int y = j - scanSize; y <= j + scanSize; y++) {
					for(int z = k - scanSize; z <= k + scanSize; z++) {
						if( Block.blocksList[id].canPlaceBlockAt(world, x, y, z) && world.getBlockMaterial(x, y, z)!=Material.water){
							if (id!=Block.tallGrass.blockID)
							{
								world.setBlock(x, y, z, id);							
							}
							else//Metadata sensitive for tall grass
							{
								world.setBlock(x, y, z, id, world.getBlockMetadata(i, j, k), 3);
							}
							return;
						}
					}
				}
			}
			return;
		case MOSS://Moss grows on both stone (or only cobblestone), changing only one block
			scanSize = 1;	
			for(int x = i - scanSize; x <= i + scanSize; x++) {
				for(int y = j - scanSize; y <= j + scanSize; y++) {
					for(int z = k - scanSize; z <= k + scanSize; z++) {
						int iD = world.getBlockId(x, y, z);
						if((mossCorruptStone && iD == Block.stone.blockID) || iD == Block.cobblestone.blockID) {
							world.setBlock(x, y, z, id);
							return;
						}
					}
				}
			}
			return;
		case MUSHROOM://Small chance of having a mushroom tree, grown using vanilla method
			if (Math.random()<(double)getGrowthProb(world, i, j, k, id+60 ,NOType.MUSHROOMCAP))
				((BlockMushroom) Block.blocksList[id]).fertilizeMushroom(world, i, j, k, world.rand);
			else//Grow a similar mushroom nearby
			{
				scanSize = 1;	
			for(int x = i - scanSize; x <= i + scanSize; x++) {
				for(int y = j - scanSize; y <= j + scanSize; y++) {
					for(int z = k - scanSize; z <= k + scanSize; z++) {
						if(((BlockMushroom) Block.blocksList[id]).canPlaceBlockAt(world, x, y, z)) {
							world.setBlock(x, y, z, id);
							return;
						}
					}
				}
			}
			}
			return;
		case FERTILIZED://Blocks that uses fertilize function for grow
			if (Block.blocksList[id] instanceof BlockStem)
				((BlockStem)Block.blocksList[id]).fertilizeStem(world, i, j, k);
			else if (Block.blocksList[id] instanceof BlockCrops)
				((BlockCrops)Block.blocksList[id]).fertilize(world, i, j, k);
			return;
		default:
			return;
		}
	}
	/**
	* Get the growth probability
	* @param	world
	* @param	i first coordinate
	* @param	j second coordinate
	* @param	k third coordinate
	* @param 	type the NOType
	* @return	Growth probability as a float
	*/
	private float getGrowthProb(World world, int i, int j, int k, int id, NOType type) {	
		float freq = type!=NOType.APPLE?getGrowthRate(id):growSets[names.length]?growthRates[names.length]*1.5F:-1F;
		if(biomeModifiedRate && freq>0 && type!=NOType.NETHERSTALK) {
			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
			if(type!=NOType.CACTUS && ((biome.rainfall == 0) || (biome.temperature > 1.5F))) {
				return 0.01F;
			} else if(type!=NOType.CUSTOM){
			freq *= Utils.getOptValueMult(biome.rainfall, type!=NOType.APPLE?getOptRain(id):0.8F, type.getRainGrowth());
			freq *= Utils.getOptValueMult(biome.temperature, type!=NOType.APPLE?getOptTemp(id):0.7F, type.getTempGrowth());
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
	* @param type 
	* @return	Death probability as a float
	*/
	private float getDeathProb(World world, int i, int j, int k, int id, NOType type) {			
		float freq = getDeathRate(id);
		if(biomeModifiedRate && freq>0 && type!=NOType.NETHERSTALK) {
			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
			if(type!=NOType.CACTUS && ((biome.rainfall == 0) || (biome.temperature > 1.5F))) {
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
	/**
	* Check if an apple can grow in this biome
	* 
	* @return	True if it can grow in these coordinates
	*/
	private boolean appleCanGrow(World world, int i, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i, k);	
		// Apples can grow in the named biomes
		return ((biome.temperature >= 0.7F) && (biome.temperature <= 1.0F) 
				&& (biome.rainfall > 0.4F));
	}
	/**
	* Check if an cocoa can grow in this biome
	*
	* @return	true if can grow in these coordinates
	*/
	private boolean cocoaCanGrow(World world, int i, int k) {
		BiomeGenBase biome = world.getBiomeGenForCoords(i,k);	
		// Cocoa can grow in the named biomes
		return ((biome.temperature >= 0.7F) && (biome.temperature <= 1.5F) 
				&& (biome.rainfall >= 0.8F));
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
		return id>0 && id<4096 && Block.blocksList[id]!=null && IDToGrowingMapping.containsKey(Integer.valueOf(id));
	}
	public HashMap<Integer, NOType> getIDToTypeMapping(){
		return IDToTypeMapping;
	}
	public HashMap<Integer, Integer> getLogToLeafMapping(){
		return LogToLeafMapping;
	}
	
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
    @PostInit//Register blocks with config values and NOType, and log/leaf couples 
    public void modsLoaded(FMLPostInitializationEvent event){
    	for (int i=1;i<Block.blocksList.length;i++)
    	{
    		if (Block.blocksList[i]!=null)
    		{
    			if (Block.blocksList[i] instanceof IGrowable && Block.blocksList[i] instanceof IBlockDeath)//Priority to Blocks using the API
    			{
    				addMapping(i,true,((IGrowable)Block.blocksList[i]).getGrowthRate(),true, ((IBlockDeath)Block.blocksList[i]).getDeathRate(),-1.0F,-1.0F,NOType.CUSTOM);      			
    			}
    			else if (Block.blocksList[i] instanceof IGrowable)
    			{
    				addMapping(i,true,((IGrowable)Block.blocksList[i]).getGrowthRate(),false,-1,-1.0F,-1.0F,NOType.CUSTOM);
    			}
    			else if (Block.blocksList[i] instanceof IBlockDeath)
    			{
    				addMapping(i, false, -1, true, ((IBlockDeath)Block.blocksList[i]).getDeathRate(), -1.0F, -1.0F,NOType.CUSTOM);
    			} 
    			else if(Block.blocksList[i] instanceof BlockSapling)
    			{
    				addMapping(i, growSets[0], 0, dieSets[0],deathRates[0], 0.8F, 0.8F,NOType.SAPLING);
    			}
    			else if(Block.blocksList[i] instanceof BlockLog)
    			{
    				addMapping(i,growSets[1],growthRates[1],dieSets[1],deathRates[1], 1.0F, 1.0F,NOType.LOG);
    			}	
    			else if(Block.blocksList[i] instanceof BlockNetherStalk)//In the Nether, we don't use biome dependent parameter
    			{
    				addMapping(i,growSets[3],growthRates[3],dieSets[3],deathRates[3],1.0F,1.0F,NOType.NETHERSTALK);
    			}			
    			else if (Block.blocksList[i] instanceof BlockGrass||Block.blocksList[i] instanceof BlockMycelium)
    			{
    				addMapping(i, growSets[4], growthRates[4], dieSets[4], deathRates[4], 0.7F, 0.5F,NOType.GRASS);
    			}
    			else if(Block.blocksList[i] instanceof BlockReed)
    			{
    				addMapping(i,growSets[5],growthRates[5],dieSets[5],deathRates[5],0.8F,0.8F,NOType.REED);
    			}
    			else if(Block.blocksList[i] instanceof BlockCactus)
    			{
    				addMapping(i, growSets[6],growthRates[6], dieSets[6],deathRates[6], 1.5F, 0.2F,NOType.CACTUS);
    			}	
    			else if (Block.blocksList[i] instanceof BlockMushroom)
    			{
    				addMapping(i,growSets[7],growthRates[7],dieSets[7],deathRates[7],0.9F,1.0F,NOType.MUSHROOM);
    			}
    			else if(Block.blocksList[i] instanceof BlockMushroomCap)
    			{
    				addMapping(i,growSets[8],growthRates[8],dieSets[8],deathRates[8],0.9F,1.0F,NOType.MUSHROOMCAP);				
    			}
    			else if(Block.blocksList[i] instanceof BlockLeaves)
    			{
    				addMapping(i, growSets[9], growthRates[9], dieSets[9],deathRates[9], 1.0F, 1.0F,NOType.LEAVES );
    			}
    			else if(Block.blocksList[i] instanceof BlockCrops || Block.blocksList[i] instanceof BlockStem)
    			{
    				addMapping(i,growSets[10],growthRates[10],dieSets[10],deathRates[10],1.0F,1.0F,NOType.FERTILIZED);
    			}
    			else if(Block.blocksList[i] instanceof BlockFlower)//Flower ,deadbush, lilypad, tallgrass
    			{
    				addMapping(i,growSets[2],growthRates[2],dieSets[2],deathRates[2],0.6F,0.7F,NOType.PLANT);
    			}			
    			else if(i==Block.cobblestoneMossy.blockID)
    			{
    				addMapping(i,growSets[11],growthRates[11],dieSets[11],deathRates[11],0.7F,1.0F,NOType.MOSS);			
    			}
    			else if(Block.blocksList[i] instanceof BlockCocoa)
    			{
    				addMapping(i,growSets[12],growthRates[12],dieSets[12],deathRates[12],1.0F,1.0F,NOType.COCOA);
    			}
    		}
    		for(int id=0;id<idMapForTree.length;id=id+2)
    		{
    			LogToLeafMapping.put(Integer.valueOf(idMapForTree[id]), Integer.valueOf(idMapForTree[id+1]));
    		}
    	}
    }
    @Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{	
    	if (tickData.length>0 && tickData[0] instanceof World)
    	{
    		//tickTimer=0;
    		World world = (World) tickData[0];
    		if((world.provider.dimensionId==0|| (customDimension && world.provider.dimensionId!=1)) && !world.activeChunkSet.isEmpty())
    		{
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
							/*ArrayList<NextTickListEntry> list= (ArrayList<NextTickListEntry>) world.getPendingBlockUpdates(chunk, false);//This is too slow, we can't call it on each tick
							if (list!=null && !list.isEmpty())
							{		
								//System.out.println("check 1");
								Iterator itr=list.iterator();						
								while(itr.hasNext())				
								{			
									NextTickListEntry nextTickEntry=(NextTickListEntry) itr.next();					
									if ( nextTickEntry.scheduledTime == world.getTotalWorldTime() && isValid(nextTickEntry.blockID))						
									{	
										//System.out.println("check 2");
										onUpdateTick(world,nextTickEntry.xCoord,nextTickEntry.yCoord,nextTickEntry.zCoord,nextTickEntry.blockID);						
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
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {}
	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}
	@Override
	public String getLabel() {
		return "Nature Overhaul Tick";
	}
}
