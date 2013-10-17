package assets.natureoverhaul;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.*;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import assets.natureoverhaul.handlers.AnimalEventHandler;
import assets.natureoverhaul.handlers.AutoFarmingEventHandler;
import assets.natureoverhaul.handlers.AutoSaplingEventHandler;
import assets.natureoverhaul.handlers.BonemealEventHandler;
import assets.natureoverhaul.handlers.PlayerEventHandler;
import assets.natureoverhaul.handlers.SaplingGrowEventHandler;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "natureoverhaul", name = "Nature Overhaul", version = "0.7", dependencies = "after:mod_MOAPI")
/**
 * From Clinton Alexander idea.
 * @author Olivier
 *
 */
public class NatureOverhaul implements ITickHandler {
	private enum GrowthType {
		NEITHER, LEAFGROWTH, LEAFDECAY, BOTH
	}

	@Instance("natureoverhaul")
	public static NatureOverhaul instance;
	private static boolean autoSapling = true, autoFarming = true, lumberjack = true, moddedBonemeal = true, killLeaves = true, biomeModifiedRate = true;
	public static boolean useStarvingSystem = true, decayLeaves = true, mossCorruptStone = true;
	private static boolean customDimension = true, wildAnimalsBreed = true;
	private static int wildAnimalBreedRate = 0, wildAnimalDeathRate = 0;
	public static int growthType = 0;
	private int updateLCG = (new Random()).nextInt();
	private static Map<Integer, NOType> IDToTypeMapping = new HashMap();
	private static Map<Integer, Boolean> IDToGrowingMapping = new HashMap(), IDToDyingMapping = new HashMap();
	private static Map<Integer, Integer> LogToLeafMapping = new HashMap(), IDToFireCatchMapping = new HashMap(), IDToFirePropagateMapping = new HashMap(), LeafToSaplingMapping = new HashMap();
	private static Map<Integer, String[]> TreeIdToMeta = new HashMap();
	private static String[] names = new String[] { "Sapling", "Tree", "Plants", "Netherwort", "Grass", "Reed", "Cactus", "Mushroom", "Mushroom Tree", "Leaf", "Crops", "Moss", "Cocoa" };
	private static boolean[] dieSets = new boolean[names.length], growSets = new boolean[names.length + 1];
	private static float[] deathRates = new float[names.length], growthRates = new float[names.length + 1];
	private static String[] optionsCategory = new String[names.length + 1];
	static {
		for (int i = 0; i < names.length; i++) {
			optionsCategory[i] = names[i] + " Options";
		}
		optionsCategory[names.length] = "Misc Options";
	};
	private static Configuration config;
	private static Class api;
	private static boolean API;
	private static BonemealEventHandler bonemealEvent;
	private static AnimalEventHandler animalEvent;
	private static PlayerEventHandler lumberEvent;
	private static AutoSaplingEventHandler autoEvent;
	private static AutoFarmingEventHandler farmingEvent;
	private static Logger logger;

	@Override
	public String getLabel() {
		return "Nature Overhaul Tick";
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		TickRegistry.registerTickHandler(this, Side.SERVER);
	}

	@EventHandler
	//Register blocks with config values and NOType, and log/leaf couples
	public void modsLoaded(FMLPostInitializationEvent event) {
		if (Loader.isModLoaded("mod_MOAPI")) {//We can use reflection to load options in MOAPI
			try {
				api = Class.forName("moapi.ModOptionsAPI");
				Method addMod = api.getMethod("addMod", String.class);
				//"addMod" is static, we don't need an instance
				Object option = addMod.invoke(null, "Nature Overhaul");
				Class optionClass = addMod.getReturnType();
				//Set options as able to be used on a server,get the instance back
				option = optionClass.getMethod("setServerMode").invoke(option);
				//"addBooleanOption" and "addSliderOption" aren't static, we need options class and an instance
				Method addBoolean = optionClass.getMethod("addBooleanOption", new Class[] { String.class, boolean.class });
				Method addSlider = optionClass.getMethod("addSliderOption", new Class[] { String.class, int.class, int.class });
				Method addMap = optionClass.getMethod("addMappedOption", new Class[] { String.class, String[].class, int[].class });
				Method setSliderValue = Class.forName("moapi.ModOptionSlider").getMethod("setValue", int.class);
				//To create a submenu
				Method addSubOption = optionClass.getMethod("addSubOption", String.class);
				//Create "General" submenu and options
				Object subOption = addSubOption.invoke(option, "General");
				Object slidOption;
				for (int i = 0; i < names.length; i++) {
					addBoolean.invoke(subOption, names[i] + " grow", true);
					addBoolean.invoke(subOption, names[i] + " die", true);
					slidOption = addSlider.invoke(subOption, names[i] + " growth rate", 0, 10000);
					setSliderValue.invoke(slidOption, 1200);
					slidOption = addSlider.invoke(subOption, names[i] + " death rate", 0, 10000);
					setSliderValue.invoke(slidOption, 1200);
				}
				addBoolean.invoke(subOption, "Apple grows", true);
				slidOption = addSlider.invoke(subOption, "Apple growth rate", 0, 10000);
				setSliderValue.invoke(slidOption, 3000);
				//Create "LumberJack" submenu and options
				Object lumberJackOption = addSubOption.invoke(option, "LumberJack");
				addBoolean.invoke(lumberJackOption, "Enable", true);
				addBoolean.invoke(lumberJackOption, "Kill leaves", true);
				//Create "Misc" submenu and options
				Object miscOption = addSubOption.invoke(option, "Misc");
				addMap.invoke(miscOption, "Sapling drops on", new String[] { "Both", "LeafDecay", "LeafGrowth", "Neither" }, new int[] { 3, 2, 1, 0 });
				addBoolean.invoke(miscOption, "AutoSapling", true);
				addBoolean.invoke(miscOption, "Plant seeds on player drop", true);
				addBoolean.invoke(miscOption, "Leaves decay on tree death", true);
				addBoolean.invoke(miscOption, "Moss growing on stone", true);
				addBoolean.invoke(miscOption, "Starving system", true);
				addBoolean.invoke(miscOption, "Biome specific rates", true);
				addBoolean.invoke(miscOption, "Modded Bonemeal", true);
				addBoolean.invoke(miscOption, "Custom dimensions", true);
				//Create "Animals" submenu and options
				Object animalsOption = addSubOption.invoke(option, "Animals");
				addBoolean.invoke(animalsOption, "Wild breed", true);
				slidOption = addSlider.invoke(animalsOption, "Breeding rate", 1, 10000);
				setSliderValue.invoke(slidOption, 10000);
				slidOption = addSlider.invoke(animalsOption, "Death rate", 1, 10000);
				setSliderValue.invoke(slidOption, 10000);
				//Create "Fire" submenu and options
				Object fireOption = addSubOption.invoke(option, "Fire");
				addSlider.invoke(fireOption, "WIP-This has no effect", 0, 100);
				//Loads and saves values
				option = optionClass.getMethod("loadValues").invoke(option);
				option = optionClass.getMethod("saveValues").invoke(option);
				//We have saved the values, we can start to get them back
				getMOAPIValues(optionClass, subOption, lumberJackOption, miscOption, animalsOption);
				//We successfully get all options !
				API = true;
				logger.finest("NatureOverhaul found MOAPI and loaded all options correctly.");
			} catch (SecurityException s) {
				API = false;
			} catch (ClassNotFoundException c) {
				API = false;
				logger.info("NatureOverhaul couldn't use MOAPI, continuing with values in config file.");
			} catch (ReflectiveOperationException n) {
				API = false;
				logger.log(Level.WARNING, "NatureOverhaul failed to use MOAPI, please report to NO author:", n);
			}//Even if it fails, we can still rely on settings stored in Forge recommended config file.
		}
		//Now we can register every available blocks at this point.
		Set<Integer> logID = new HashSet(), leafID = new HashSet(), saplingID = new HashSet();
		//If a block is registered after, it won't be accounted for.
		for (int i = 1; i < Block.blocksList.length; i++) {
			if (Block.blocksList[i] != null) {
				if (Block.blocksList[i] instanceof IGrowable && Block.blocksList[i] instanceof IBlockDeath) {//Priority to Blocks using the API
					addMapping(i, true, ((IGrowable) Block.blocksList[i]).getGrowthRate(), true, ((IBlockDeath) Block.blocksList[i]).getDeathRate(), -1.0F, -1.0F, NOType.CUSTOM);
				} else if (Block.blocksList[i] instanceof IGrowable) {
					addMapping(i, true, ((IGrowable) Block.blocksList[i]).getGrowthRate(), false, -1, -1.0F, -1.0F, NOType.CUSTOM);
				} else if (Block.blocksList[i] instanceof IBlockDeath) {
					addMapping(i, false, -1, true, ((IBlockDeath) Block.blocksList[i]).getDeathRate(), -1.0F, -1.0F, NOType.CUSTOM);
				} else if (Block.blocksList[i] instanceof BlockSapling) {
					saplingID.add(i);
				} else if (Block.blocksList[i] instanceof BlockLog) {
					logID.add(i);
				} else if (Block.blocksList[i] instanceof BlockNetherStalk) {//In the Nether, we don't use biome dependent parameter
					addMapping(i, growSets[3], growthRates[3], dieSets[3], deathRates[3], 1.0F, 1.0F, NOType.NETHERSTALK);
				} else if (Block.blocksList[i] instanceof BlockGrass || Block.blocksList[i] instanceof BlockMycelium) {
					addMapping(i, growSets[4], growthRates[4], dieSets[4], deathRates[4], 0.7F, 0.5F, NOType.GRASS);
				} else if (Block.blocksList[i] instanceof BlockReed) {
					addMapping(i, growSets[5], growthRates[5], dieSets[5], deathRates[5], 0.8F, 0.8F, NOType.REED);
				} else if (Block.blocksList[i] instanceof BlockCactus) {
					addMapping(i, growSets[6], growthRates[6], dieSets[6], deathRates[6], 1.5F, 0.2F, NOType.CACTUS);
				} else if (Block.blocksList[i] instanceof BlockMushroom) {
					addMapping(i, growSets[7], growthRates[7], dieSets[7], deathRates[7], 0.9F, 1.0F, NOType.MUSHROOM);
				} else if (Block.blocksList[i] instanceof BlockMushroomCap) {
					addMapping(i, growSets[8], growthRates[8], dieSets[8], deathRates[8], 0.9F, 1.0F, NOType.MUSHROOMCAP);
				} else if (Block.blocksList[i] instanceof BlockLeaves) {
					leafID.add(i);
				} else if (Block.blocksList[i] instanceof BlockCrops || Block.blocksList[i] instanceof BlockStem) {
					addMapping(i, growSets[10], growthRates[10], dieSets[10], deathRates[10], 1.0F, 1.0F, NOType.FERTILIZED);
				} else if (Block.blocksList[i] instanceof BlockFlower) {//Flower ,deadbush, lilypad, tallgrass
					addMapping(i, growSets[2], growthRates[2], dieSets[2], deathRates[2], 0.6F, 0.7F, NOType.PLANT, 100, 60);
				} else if (i == Block.cobblestoneMossy.blockID) {
					addMapping(i, growSets[11], growthRates[11], dieSets[11], deathRates[11], 0.7F, 1.0F, NOType.MOSS);
				} else if (Block.blocksList[i] instanceof BlockCocoa) {
					addMapping(i, growSets[12], growthRates[12], dieSets[12], deathRates[12], 1.0F, 1.0F, NOType.COCOA);
				}
				if (Block.blocksList[i].isCollidable() && Block.blocksList[i].renderAsNormalBlock()) {
					IDToFirePropagateMapping.put(
							i,
							config.get("Fire", Block.blocksList[i].getUnlocalizedName().substring(5) + " chance to encourage fire",
									IDToFirePropagateMapping.containsKey(i) ? IDToFirePropagateMapping.get(i) : 0).getInt());
					IDToFireCatchMapping.put(i,
							config.get("Fire", Block.blocksList[i].getUnlocalizedName().substring(5) + " chance to catch fire", IDToFireCatchMapping.containsKey(i) ? IDToFireCatchMapping.get(i) : 0)
									.getInt());
					Block.setBurnProperties(i, IDToFirePropagateMapping.get(i), IDToFireCatchMapping.get(i));
				}
			}
		}
		int[] idLog = Ints.toArray(logID), idLeaf = Ints.toArray(leafID), idSapling = Ints.toArray(saplingID);
		String option = "";
		for (int index = 0; index < Math.min(Math.min(idLog.length, idLeaf.length), idSapling.length); index++) {
			option = option.concat(idSapling[index] + "(0,1,2,3)-" + idLog[index] + "(0,1,2,3)-" + idLeaf[index] + "(0,1,2,3);");
		}
		String[] ids = config.get(optionsCategory[names.length], "Sapling-Log-Leaves ids", option).getString().split(";");
		String[] temp;
		for (String param : ids) {
			if (param != null && param != "") {
				temp = param.split("-");
				if (temp.length == 3) {
					int idSaplin, idLo, idLef;
					try {
						idSaplin = Integer.parseInt(temp[0].split("\\(")[0]);
						idLo = Integer.parseInt(temp[1].split("\\(")[0]);
						idLef = Integer.parseInt(temp[2].split("\\(")[0]);
					} catch (NumberFormatException e) {
						continue;
					}
					//Make sure user input is valid
					if (isValid(idSaplin) && isValid(idLo) && isValid(idLef)) {
						addMapping(idSaplin, growSets[0], 0, dieSets[0], deathRates[0], 0.8F, 0.8F, NOType.SAPLING);
						TreeIdToMeta.put(idSaplin, temp[0].split("\\(")[1].replace("\\)", "").trim().split("\\,"));
						addMapping(idLo, growSets[1], growthRates[1], dieSets[1], deathRates[1], 1.0F, 1.0F, NOType.LOG, 5, 5);
						TreeIdToMeta.put(idLo, temp[1].split("\\(")[1].replace("\\)", "").trim().split("\\,"));
						addMapping(idLef, growSets[9], growthRates[9], dieSets[9], deathRates[9], 1.0F, 1.0F, NOType.LEAVES, 60, 30);
						TreeIdToMeta.put(idLef, temp[2].split("\\(")[1].replace("\\)", "").trim().split("\\,"));
						LogToLeafMapping.put(idLo, idLef);
						LeafToSaplingMapping.put(idLef, idSaplin);
					}
				}
			}
		}
		//Saving Forge recommended config file.
		if (config.hasChanged()) {
			config.save();
		}
		//Registering event listeners.
		bonemealEvent = new BonemealEventHandler(moddedBonemeal);
		MinecraftForge.EVENT_BUS.register(bonemealEvent);
		animalEvent = new AnimalEventHandler(wildAnimalsBreed, wildAnimalBreedRate, wildAnimalDeathRate);
		MinecraftForge.EVENT_BUS.register(animalEvent);
		lumberEvent = new PlayerEventHandler(lumberjack, killLeaves);
		MinecraftForge.EVENT_BUS.register(lumberEvent);
		farmingEvent = new AutoFarmingEventHandler(autoFarming);
		MinecraftForge.EVENT_BUS.register(farmingEvent);
		autoEvent = new AutoSaplingEventHandler(autoSapling);
		MinecraftForge.EVENT_BUS.register(autoEvent);
		if (growthType % 2 == 0)
			MinecraftForge.EVENT_BUS.register(new SaplingGrowEventHandler());
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		config = new Configuration(event.getSuggestedConfigurationFile(), true);
		config.load();
		for (String name : optionsCategory) {
			config.addCustomCategoryComment(name, "The lower the rate, the faster the changes happen.");
		}
		config.addCustomCategoryComment(optionsCategory[2], "Plants are flower, deadbush, lilypad and tallgrass");
		autoSapling = config.get(optionsCategory[0], "AutoSapling", true).getBoolean(true);
		for (int i = 0; i < names.length; i++) {
			dieSets[i] = config.get(optionsCategory[i], names[i] + " Die", true).getBoolean(true);
			growSets[i] = config.get(optionsCategory[i], names[i] + " Grow", true).getBoolean(true);
			deathRates[i] = config.get(optionsCategory[i], names[i] + " Death Rate", 1200).getInt(1200);
			growthRates[i] = config.get(optionsCategory[i], names[i] + " Growth Rate", 1200).getInt(1200);
		}
		//Toggle between alternative time of growth for sapling
		growthType = GrowthType.valueOf(config.get(optionsCategory[0], "Sapling drops on", "Both", "Possible values are Neither,LeafGrowth,LeafDecay,Both").getString().toUpperCase()).ordinal();
		//Toggle for lumberjack system on trees
		lumberjack = config.get(optionsCategory[1], "Enable lumberjack", true).getBoolean(true);
		killLeaves = config.get(optionsCategory[1], "Lumberjack kill leaves", true).getBoolean(true);
		//Apples don't have a dying system, because it is only an item
		growSets[names.length] = config.get(optionsCategory[9], "Apple Grows", true).getBoolean(true);
		growthRates[names.length] = config.get(optionsCategory[9], "Apple Growth Rate", 3000).getInt(3000);
		//Force remove leaves after killing a tree, instead of letting Minecraft doing it
		decayLeaves = config.get(optionsCategory[9], "Enable leaves decay on tree death", true).getBoolean(true);
		//Toggle so Stone can turn into Mossy Cobblestone
		mossCorruptStone = config.get(optionsCategory[11], "Enable moss growing on stone", true).getBoolean(true);
		//Misc options
		useStarvingSystem = config.get(optionsCategory[names.length], "Enable starving system", true).getBoolean(true);
		biomeModifiedRate = config.get(optionsCategory[names.length], "Enable biome specific rates", true).getBoolean(true);
		moddedBonemeal = config.get(optionsCategory[names.length], "Enable modded Bonemeal", true).getBoolean(true);
		customDimension = config.get(optionsCategory[names.length], "Enable custom dimensions", true).getBoolean(true);
		wildAnimalsBreed = config.get(optionsCategory[names.length], "Enable wild animals Breed", true).getBoolean(true);
		wildAnimalBreedRate = config.get(optionsCategory[names.length], "Wild animals breed rate", 16000).getInt(16000);
		wildAnimalDeathRate = config.get(optionsCategory[names.length], "Wild animals death rate", 16000).getInt(16000);
		autoFarming = config.get(optionsCategory[names.length], "Plant seeds on player drop", true).getBoolean(true);
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);//The only TickType we want to get the world ticks.
	}

	@Override
	/**
	 * Core method. We make vanilla-like random ticks in loaded chunks.
	 */
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (API) {
			try {
				Method getMod = api.getMethod("getModOptions", String.class);
				//"getMod" is static, we don't need an instance
				Object option = getMod.invoke(null, "Nature Overhaul");
				Class optionClass = getMod.getReturnType();
				//To get a submenu
				Method getSubOption = optionClass.getMethod("getOption", String.class);
				Object subOption = getSubOption.invoke(option, "General");
				//Get "LumberJack" submenu
				Object lumberJackOption = getSubOption.invoke(option, "LumberJack");
				//Get "Misc" submenu
				Object miscOption = getSubOption.invoke(option, "Misc");
				//Get "Animals" submenu
				Object animalsOption = getSubOption.invoke(option, "Animals");
				//We can start to get the values back
				getMOAPIValues(optionClass, subOption, lumberJackOption, miscOption, animalsOption);
			} catch (SecurityException s) {
				API = false;
			} catch (ReflectiveOperationException i) {
				API = false;
			}
			bonemealEvent.set(moddedBonemeal);
			animalEvent.set(wildAnimalsBreed, wildAnimalBreedRate, wildAnimalDeathRate);
			lumberEvent.set(lumberjack, killLeaves);
			autoEvent.set(autoSapling);
			farmingEvent.set(autoFarming);
			int index = -1;
			for (int i : IDToTypeMapping.keySet()) {
				index = IDToTypeMapping.get(Integer.valueOf(i)).getIndex();
				if (index > -1) {
					Behavior behav = BehaviorManager.getBehavior(i);
					float[] data = behav.getData();
					if (growSets[index] != IDToGrowingMapping.get(Integer.valueOf(i)))
						IDToGrowingMapping.put(Integer.valueOf(i), growSets[index]);
					if (dieSets[index] != IDToDyingMapping.get(Integer.valueOf(i)))
						IDToDyingMapping.put(Integer.valueOf(i), dieSets[index]);
					if (growthRates[index] != data[0] || deathRates[index] != data[1]) {
						data[0] = growthRates[index];
						data[1] = deathRates[index];
						behav.setData(data);
						BehaviorManager.setBehavior(i, behav);
					}
				}
			}
		}
		if (tickData.length > 0 && tickData[0] instanceof WorldServer) {
			WorldServer world = (WorldServer) tickData[0];
			if ((world.provider.dimensionId == 0 || (customDimension && world.provider.dimensionId != 1)) && !world.activeChunkSet.isEmpty()) {
				Iterator it = world.activeChunkSet.iterator();
				while (it.hasNext()) {
					ChunkCoordIntPair chunkIntPair = (ChunkCoordIntPair) it.next();
					int k = chunkIntPair.chunkXPos * 16;
					int l = chunkIntPair.chunkZPos * 16;
					Chunk chunk = null;
					if (world.getChunkProvider().chunkExists(chunkIntPair.chunkXPos, chunkIntPair.chunkZPos)) {
						chunk = world.getChunkFromChunkCoords(chunkIntPair.chunkXPos, chunkIntPair.chunkZPos);
					}
					if (chunk != null && chunk.isChunkLoaded && chunk.isTerrainPopulated) {
						int i2, k2, l2, i3, j3;//Vanilla like random ticks for blocks
						for (ExtendedBlockStorage blockStorage : chunk.getBlockStorageArray()) {
							if (blockStorage != null && !blockStorage.isEmpty() && blockStorage.getNeedsRandomTick()) {
								for (int j2 = 0; j2 < 3; ++j2) {
									this.updateLCG = this.updateLCG * 3 + 1013904223;
									i2 = this.updateLCG >> 2;
									k2 = i2 & 15;
									l2 = i2 >> 8 & 15;
									i3 = i2 >> 16 & 15;
									j3 = blockStorage.getExtBlockID(k2, i3, l2);
									if (isRegistered(j3)) {
										onUpdateTick(world, k2 + k, i3 + blockStorage.getYLocation(), l2 + l, j3);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * The death general method. Called by
	 * {@link #onUpdateTick(World, int, int, int, int)} when conditions are
	 * fulfilled.
	 **/
	public static void death(World world, int i, int j, int k, int id) {
		if (Block.blocksList[id] instanceof IBlockDeath) {
			((IBlockDeath) Block.blocksList[id]).death(world, i, j, k, id);
		} else {
			BehaviorManager.getBehavior(id, null).death(world, i, j, k, id);
		}
	}

	/**
	 * Special case for apples, since they don't have a corresponding block
	 *
	 * @return apple growth probability at given coordinates
	 */
	public static float getAppleGrowthProb(World world, int i, int j, int k) {
		float freq = growSets[names.length] ? growthRates[names.length] * 1.5F : -1F;
		if (biomeModifiedRate && freq > 0) {
			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
			if (biome.rainfall == 0 || biome.temperature > 1.5F) {
				return 0.01F;
			} else {
				freq *= Utils.getOptValueMult(biome.rainfall, 0.8F, 4.0F);
				freq *= Utils.getOptValueMult(biome.temperature, 0.7F, 4.0F);
			}
		}
		if (freq > 0)
			return 1F / freq;
		else
			return -1F;
	}

	/**
	 * Get the growth probability. Called by
	 * {@link #onUpdateTick(World, int, int, int, int)}.
	 *
	 * @return growth probability for given blockid and NOType at given
	 *         coordinates
	 */
	public static float getGrowthProb(World world, int i, int j, int k, int id, NOType type) {
		float freq = getGrowthRate(id);
		if (biomeModifiedRate && freq > 0 && type != NOType.NETHERSTALK) {
			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
			if (type != NOType.CACTUS && ((biome.rainfall == 0) || (biome.temperature > 1.5F))) {
				return 0.01F;
			} else if (type != NOType.CUSTOM) {
				freq *= Utils.getOptValueMult(biome.rainfall, getOptRain(id), type.getRainGrowth());
				freq *= Utils.getOptValueMult(biome.temperature, getOptTemp(id), type.getTempGrowth());
			}
		}
		if (freq > 0)
			return 1F / freq;
		else
			return -1F;
	}

	public static Map<Integer, NOType> getIDToTypeMapping() {
		return ImmutableMap.copyOf(IDToTypeMapping);
	}

	public static Map<Integer, Integer> getLeafToSaplingMapping() {
		return ImmutableMap.copyOf(LeafToSaplingMapping);
	}

	public static Map<Integer, Integer> getLogToLeafMapping() {
		return ImmutableMap.copyOf(LogToLeafMapping);
	}

	public static Map<Integer, String[]> getTreeIDMeta() {
		return ImmutableMap.copyOf(TreeIdToMeta);
	}

	/**
	 * The general growing method. Called by
	 * {@link #onUpdateTick(World, int, int, int, int)}. when conditions are
	 * fulfilled.
	 **/
	public static void grow(World world, int i, int j, int k, int id) {
		if (Block.blocksList[id] instanceof IGrowable) {
			((IGrowable) Block.blocksList[id]).grow(world, i, j, k, id);
		} else {
			BehaviorManager.getBehavior(Integer.valueOf(id)).grow(world, i, j, k, id);
		}
	}

	/**
	 * Check if given block id is registered as growing
	 *
	 * @param id
	 *            the block id to check
	 * @return true if block can grow
	 */
	public static boolean isGrowing(int id) {
		return IDToGrowingMapping.get(Integer.valueOf(id));
	}

	/**
	 * Check if given block id is registered as a log (may be part of a tree)
	 *
	 * @param id
	 *            the block id to check
	 * @return true if block is a log
	 */
	public static boolean isLog(int id) {
		return LogToLeafMapping.containsKey(id) || (isRegistered(id) && IDToTypeMapping.get(id) == NOType.MUSHROOMCAP);
	}

	public static boolean isRegistered(int id) {
		return isValid(id) && IDToTypeMapping.containsKey(Integer.valueOf(id));
	}

	public static boolean isValid(int id) {
		return id > 0 && id < 4096 && Block.blocksList[id] != null;
	}

	/**
	 * Registers all mappings simultaneously.
	 *
	 * @param id
	 *            The id the block is registered with.
	 * @param isGrowing
	 *            Whether the block can call
	 *            {@link #grow(World, int, int, int, int, NOType)} on tick.
	 * @param growthRate
	 *            How often the {@link #grow(World, int, int, int, int, NOType)}
	 *            method will be called.
	 * @param isMortal
	 *            Whether the block can call
	 *            {@link #death(World, int, int, int, int, NOType)} method on
	 *            tick.
	 * @param deathRate
	 *            How often the
	 *            {@link #death(World, int, int, int, int, NOType)} method will
	 *            be called.
	 * @param optTemp
	 *            The optimal temperature parameter for the growth.
	 * @param optRain
	 *            The optimal humidity parameter for the growth.
	 * @param type
	 *            {@link NOType} Decides which growth and/or death to use, and
	 *            tolerance to temperature and humidity.
	 */
	private static void addMapping(int id, boolean isGrowing, float growthRate, boolean isMortal, float deathRate, float optTemp, float optRain, NOType type) {
		IDToGrowingMapping.put(Integer.valueOf(id), isGrowing);
		IDToDyingMapping.put(Integer.valueOf(id), isMortal);
		IDToTypeMapping.put(Integer.valueOf(id), type);
		BehaviorManager.setBehavior(Integer.valueOf(id), BehaviorManager.getBehavior(type).setData(growthRate, deathRate, optRain, optTemp));
	}

	/**
	 * Registers all mappings simultaneously. Overloaded method with fire
	 * parameters.
	 *
	 * @param fireCatch
	 *            Related to 3rd parameter in {@link
	 *            Block.setBurnProperties(int,int,int)}.
	 * @param firePropagate
	 *            Related to 2nd parameter in {@link
	 *            Block.setBurnProperties(int,int,int)}.
	 */
	private static void addMapping(int id, boolean isGrowing, float growthRate, boolean isMortal, float deathRate, float optTemp, float optRain, NOType type, int fireCatch, int firePropagate) {
		addMapping(id, isGrowing, growthRate, isMortal, deathRate, optTemp, optRain, type);
		IDToFireCatchMapping.put(Integer.valueOf(id), fireCatch);
		IDToFirePropagateMapping.put(Integer.valueOf(id), firePropagate);
	}

	/**
	 * Helper reflection method for booleans
	 */
	private static boolean getBooleanFrom(Method meth, Object option, String name) throws ReflectiveOperationException {
		return Boolean.class.cast(meth.invoke(option, name)).booleanValue();
	}

	/**
	 * Get the death probability. Called by
	 * {@link #onUpdateTick(World, int, int, int, int)}.
	 *
	 * @return Death probability for given blockid and NOType at given
	 *         coordinates
	 */
	private static float getDeathProb(World world, int i, int j, int k, int id, NOType type) {
		float freq = getDeathRate(id);
		if (biomeModifiedRate && freq > 0 && type != NOType.NETHERSTALK) {
			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
			if (type != NOType.CACTUS && ((biome.rainfall == 0) || (biome.temperature > 1.5F))) {
				return 1F;
			} else if (type != NOType.CUSTOM) {
				freq *= Utils.getOptValueMult(biome.rainfall, getOptRain(id), type.getRainDeath());
				freq *= Utils.getOptValueMult(biome.temperature, getOptTemp(id), type.getTempDeath());
			}
		}
		if (freq > 0)
			return 1F / freq;
		else
			return -1F;
	}

	private static float getDeathRate(int id) {
		if (Block.blocksList[id] instanceof IBlockDeath) {
			return ((IBlockDeath) Block.blocksList[id]).getDeathRate();
		} else {
			return BehaviorManager.getBehavior(Integer.valueOf(id)).getDeathRate();
		}
	}

	private static float getGrowthRate(int id) {
		if (Block.blocksList[id] instanceof IGrowable) {
			return ((IGrowable) Block.blocksList[id]).getGrowthRate();
		} else {
			return BehaviorManager.getBehavior(Integer.valueOf(id)).getGrowthRate();
		}
	}

	private static int getIntFrom(Method meth, Object obj, String name) throws ReflectiveOperationException {
		return Integer.class.cast(meth.invoke(obj, name)).intValue();
	}

	private static void getMOAPIValues(Class optionClass, Object subOption, Object lumberJackOption, Object miscOption, Object animalsOption) throws SecurityException, ReflectiveOperationException {
		Method getBoolean = optionClass.getMethod("getBooleanValue", String.class);
		Method getSlider = optionClass.getMethod("getSliderValue", String.class);
		Method getMap = optionClass.getMethod("getMappedValue", String.class);
		for (int i = 0; i < names.length; i++) {
			growSets[i] = getBooleanFrom(getBoolean, subOption, names[i] + " grow");
			dieSets[i] = getBooleanFrom(getBoolean, subOption, names[i] + " die");
			growthRates[i] = getIntFrom(getSlider, subOption, names[i] + " growth rate");
			deathRates[i] = getIntFrom(getSlider, subOption, names[i] + " death rate");
		}
		growSets[names.length] = getBooleanFrom(getBoolean, subOption, "Apple grows");
		growthRates[names.length] = getIntFrom(getSlider, subOption, "Apple growth rate");
		lumberjack = getBooleanFrom(getBoolean, lumberJackOption, "Enable");
		killLeaves = getBooleanFrom(getBoolean, lumberJackOption, "Kill leaves");
		growthType = getIntFrom(getMap, miscOption, "Sapling drops on");
		autoSapling = getBooleanFrom(getBoolean, miscOption, "AutoSapling");
		autoFarming = getBooleanFrom(getBoolean, miscOption, "Plant seeds on player drop");
		decayLeaves = getBooleanFrom(getBoolean, miscOption, "Leaves decay on tree death");
		mossCorruptStone = getBooleanFrom(getBoolean, miscOption, "Moss growing on stone");
		useStarvingSystem = getBooleanFrom(getBoolean, miscOption, "Starving system");
		biomeModifiedRate = getBooleanFrom(getBoolean, miscOption, "Biome specific rates");
		moddedBonemeal = getBooleanFrom(getBoolean, miscOption, "Modded Bonemeal");
		customDimension = getBooleanFrom(getBoolean, miscOption, "Custom dimensions");
		wildAnimalsBreed = getBooleanFrom(getBoolean, animalsOption, "Wild breed");
		wildAnimalBreedRate = getIntFrom(getSlider, animalsOption, "Breeding rate");
		wildAnimalDeathRate = getIntFrom(getSlider, animalsOption, "Death rate");
	}

	private static float getOptRain(int id) {
		return BehaviorManager.getBehavior(Integer.valueOf(id)).getOptRain();
	}

	private static float getOptTemp(int id) {
		return BehaviorManager.getBehavior(Integer.valueOf(id)).getOptTemp();
	}

	/**
	 * Called by {@link #onUpdateTick(World, int, int, int, int)}. Checks
	 * whether this block has died on this tick for any reason
	 *
	 * @return True if plant has died
	 */
	private static boolean hasDied(World world, int i, int j, int k, int id) {
		if (Block.blocksList[id] instanceof IBlockDeath) {
			return ((IBlockDeath) Block.blocksList[id]).hasDied(world, i, j, k, id);
		} else {
			return BehaviorManager.getBehavior(Integer.valueOf(id)).hasDied(world, i, j, k, id);
		}
	}

	private static boolean isMortal(int id) {
		return IDToDyingMapping.get(Integer.valueOf(id));
	}

	/**
	 * Called from the world tick {@link #tickStart(EnumSet, Object...)} with a
	 * {@link #isValid(int)} id. Checks with {@link #isGrowing(int)} or
	 * {@link #isMortal(int)} booleans, and probabilities with
	 * {@link #getGrowthProb(World, int, int, int, int, NOType)} or
	 * {@link #getDeathProb(World, int, int, int, int, NOType)} then call
	 * {@link #grow(World, int, int, int, int)} or
	 * {@link #death(World, int, int, int, int)}.
	 */
	private static void onUpdateTick(World world, int i, int j, int k, int id) {
		NOType type = Utils.getType(id);
		if (isGrowing(id) && world.rand.nextFloat() < getGrowthProb(world, i, j, k, id, type)) {
			//System.out.println("Block "+id+" growing at "+i+","+j+","+k);
			grow(world, i, j, k, id);
			return;
		}
		if (isMortal(id) && (hasDied(world, i, j, k, id) || world.rand.nextFloat() < getDeathProb(world, i, j, k, id, type))) {
			//System.out.println("Block "+id+" dying at "+i+","+j+","+k);
			death(world, i, j, k, id);
		}
	}
}
