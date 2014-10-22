package natureoverhaul;

import java.lang.reflect.Method;
import java.util.*;

import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import natureoverhaul.behaviors.BehaviorFire;
import natureoverhaul.behaviors.BehaviorMoss;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.ImmutableMap;

import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

/**
 * From Clinton Alexander idea.
 * @author Olivier
 *
 */
@Mod(modid = "natureoverhaul", name = "Nature Overhaul", useMetadata = true, dependencies = "after:mod_MOAPI", acceptableRemoteVersions = "*", guiFactory = "natureoverhaul.ConfigGuiHandler")
public class NatureOverhaul {
    private enum GrowthType {
		NEITHER, LEAFGROWTH, LEAFDECAY, BOTH
	}
    @Mod.Instance("natureoverhaul")
    public static NatureOverhaul INSTANCE;

    public boolean autoSapling = true, autoFarming = true, lumberjack = true, moddedBonemeal = true, killLeaves = true, biomeModifiedRate = true;
    public boolean useStarvingSystem = true, decayLeaves = true, mossCorruptStone = true, customDimension = true, wildAnimalsBreed = true;
	public int wildAnimalBreedRate = 0, wildAnimalDeathRate = 0, growthType = 0, fireRange = 2, despawnTimeSapling = 6000;
    private ArrayList<Item> axes = new ArrayList<Item>();
	private static Map<Block, NOType> IDToTypeMapping = new IdentityHashMap<Block, NOType>();
	private static Map<Block, Boolean> IDToGrowingMapping = new IdentityHashMap<Block, Boolean>(), IDToDyingMapping = new IdentityHashMap<Block, Boolean>();
    private static Map<Block, Integer> IDToFireCatchMapping = new IdentityHashMap<Block, Integer>(), IDToFirePropagateMapping = new IdentityHashMap<Block, Integer>();
	private static final String[] names = new String[] { "Sapling", "Tree", "Plants", "Netherwort", "Grass", "Reed", "Cactus", "Mushroom", "Mushroom Tree", "Leaf", "Crops", "Moss", "Cocoa", "Fire" };
	private boolean[] dieSets = new boolean[names.length], growSets = new boolean[names.length + 1];
	private float[] deathRates = new float[names.length], growthRates = new float[names.length + 1];
	private static String[] optionsCategory = new String[names.length + 1];
	static {
		for (int i = 0; i < names.length; i++) {
			optionsCategory[i] = names[i] + " Options";
		}
		optionsCategory[names.length] = "Misc Options";
	}
	private NOConfiguration config;
	private Class<?> api;
    private int updateLCG = (new Random()).nextInt();
	private Logger logger;

    /**
     * Register main tick and config change handler
     */
	@EventHandler
	public void load(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(this);
	}

    /*
     * Register blocks with config values and NOType, and log/leaf couples
     */
	@EventHandler
	public void modsLoaded(FMLPostInitializationEvent event) {
		if (Loader.isModLoaded("mod_MOAPI")) {//We can use reflection to load options in MOAPI
            tryUseMOAPI();
		}
		//Now we can register every available blocks at this point.
		ArrayList<Block> logID = new ArrayList<Block>(), leafID = new ArrayList<Block>(), saplingID = new ArrayList<Block>();
		//If a block is registered after, it won't be accounted for.
        Block i = null;
		for (Iterator itr=GameData.getBlockRegistry().iterator();itr.hasNext(); i = (Block)itr.next()) {
			if (i != null) {
				if (i instanceof IGrowable && i instanceof IBlockDeath) {//Priority to Blocks using the api
					addMapping(i, true, ((IGrowable) i).getGrowthRate(), true, ((IBlockDeath) i).getDeathRate(), -1.0F, -1.0F, NOType.CUSTOM);
				} else if (i instanceof IGrowable) {
					addMapping(i, true, ((IGrowable) i).getGrowthRate(), false, -1, -1.0F, -1.0F, NOType.CUSTOM);
				} else if (i instanceof IBlockDeath) {
					addMapping(i, false, -1, true, ((IBlockDeath) i).getDeathRate(), -1.0F, -1.0F, NOType.CUSTOM);
				} else if (i instanceof BlockSapling) {
					saplingID.add(i);
				} else if (i instanceof BlockLog) {
					logID.add(i);
				} else if (i instanceof BlockNetherWart) {//In the Nether, we don't use biome dependent parameter
					addMapping(i, growSets[3], growthRates[3], dieSets[3], deathRates[3], 0.0F, 0.0F, NOType.NETHERSTALK);
				} else if (i instanceof BlockGrass || i instanceof BlockMycelium) {
					addMapping(i, growSets[4], growthRates[4], dieSets[4], deathRates[4], 0.7F, 0.5F, NOType.GRASS);
				} else if (i instanceof BlockReed) {
					addMapping(i, growSets[5], growthRates[5], dieSets[5], deathRates[5], 0.8F, 0.8F, NOType.REED);
				} else if (i instanceof BlockCactus) {
					addMapping(i, growSets[6], growthRates[6], dieSets[6], deathRates[6], 1.5F, 0.2F, NOType.CACTUS);
				} else if (i instanceof BlockMushroom) {
					addMapping(i, growSets[7], growthRates[7], dieSets[7], deathRates[7], 0.9F, 1.0F, NOType.MUSHROOM);
				} else if (i instanceof BlockHugeMushroom) {
					addMapping(i, growSets[8], growthRates[8], dieSets[8], deathRates[8], 0.9F, 1.0F, NOType.MUSHROOMCAP);
				} else if (i instanceof BlockLeavesBase) {
					leafID.add(i);
				} else if (i instanceof BlockCrops || i instanceof BlockStem) {
					addMapping(i, growSets[10], growthRates[10], dieSets[10], deathRates[10], 1.0F, 1.0F, NOType.FERTILIZED);
				} else if (i instanceof BlockBush) {//Flowers, deadbush, lilypad, tallgrass
					addMapping(i, growSets[2], growthRates[2], dieSets[2], deathRates[2], 0.6F, 0.7F, NOType.PLANT, 100, 60);
				} else if (BehaviorMoss.isMossyBlock(i)) {
					addMapping(i, growSets[11], growthRates[11], dieSets[11], deathRates[11], 0.7F, 1.0F, NOType.MOSS);
				} else if (i instanceof BlockCocoa) {
					addMapping(i, growSets[12], growthRates[12], dieSets[12], deathRates[12], 1.0F, 1.0F, NOType.COCOA);
				} else if (i instanceof BlockFire) {
					addMapping(i, growSets[13], 0, dieSets[13], 0, 0.0F, 0.0F, NOType.CUSTOM);
					BehaviorManager.setBehavior(i, new BehaviorFire().setData(growthRates[13], deathRates[13]));
				}
				if (i.getMaterial().isOpaque() && i.renderAsNormalBlock() && i.isCollidable()) {
					IDToFirePropagateMapping.put(
							i,
							config.getInt(optionsCategory[13] + ".Spreading", i.getUnlocalizedName().substring(5),
                                    Blocks.fire.getEncouragement(i)));
					IDToFireCatchMapping.put(
							i,
							config.getInt(optionsCategory[13]+".Flammability", i.getUnlocalizedName().substring(5),
									Blocks.fire.getFlammability(i)));
				}
			}
		}
		StringBuilder option = new StringBuilder();
		for (int index = 0; index < logID.size() || index < leafID.size() || index < saplingID.size(); index++) {
            Block sapling = saplingID.get(index<saplingID.size()?index:0);
            Block log = logID.get(index<logID.size()?index:0);
            Block leaf = leafID.get(index<leafID.size()?index:0);
            Set<Integer> sapData = new HashSet<Integer>();
			for (int meta = 0; meta < 16; meta++) {
				sapData.add(sapling.damageDropped(meta));
			}
            String gData = GameData.getBlockRegistry().getNameForObject(log);
            String fData = GameData.getBlockRegistry().getNameForObject(leaf);
            for (int meta : sapData) {
                if(meta>3){
                    gData = GameData.getBlockRegistry().getNameForObject(logID.get(index+1<logID.size()?index+1:1));
                    fData = GameData.getBlockRegistry().getNameForObject(leafID.get(index+1<leafID.size()?index+1:1));
                }
                StringBuilder tempData = new StringBuilder("(").append(meta%4).append(",").append(meta%4+4).append(",").append(meta%4+8).append(",").append(meta%4+12);
			    option.append(GameData.getBlockRegistry().getNameForObject(sapling)).append("(").append(meta).append(")-").append(gData).append(tempData).append(")-").append(fData).append(tempData).append(");");
            }
		}
		String[] ids = config.get(optionsCategory[names.length], "Sapling-Log-Leaves names", option.toString(), "Separate groups with ;").getString().split(";");
		String[] temp;
		for (String param : ids) {
			if (param != null && !param.equals("")) {
				temp = param.split("-");
				if (temp.length == 3) {
					Block idSaplin, idLo, idLef;
					try {
						idSaplin = GameData.getBlockRegistry().getObject(temp[0].split("\\(")[0]);
						idLo = GameData.getBlockRegistry().getObject(temp[1].split("\\(")[0]);
						idLef = GameData.getBlockRegistry().getObject(temp[2].split("\\(")[0]);
					} catch (Exception e) {
						continue;
					}
					//Make sure user input is valid
					if (idSaplin!=Blocks.air && idLo!=Blocks.air && idLef!=Blocks.air) {
                        String[] sapMeta = temp[0].split("\\(")[1].split("\\)")[0].split(",");
                        String[] logMeta = temp[1].split("\\(")[1].split("\\)")[0].split(",");
						String[] lefMeta = temp[2].split("\\(")[1].split("\\)")[0].split(",");
                        for(String meta0:sapMeta){
                            try {
                                int a = Integer.parseInt(meta0.trim());
                                for (String meta1 : logMeta) {
                                    try {
                                        int o = Integer.parseInt(meta1.trim());
                                        for (String meta2 : lefMeta) {
                                            try {
                                                int e = Integer.parseInt(meta2.trim());
                                                new TreeData(idSaplin, idLo, idLef, a, o, e).register();
                                            }catch (NumberFormatException ignored){
                                            }
                                        }
                                    }catch (NumberFormatException ignored){
                                    }
                                }
                            }catch (NumberFormatException ignored){
                            }
                        }
                        if(IDToTypeMapping.get(idSaplin)==null)
                            addMapping(idSaplin, growSets[0], 0, dieSets[0], deathRates[0], 0.8F, 0.8F, NOType.SAPLING);
                        if(IDToTypeMapping.get(idLo)==null)
                            addMapping(idLo, growSets[1], growthRates[1], dieSets[1], deathRates[1], 1.0F, 1.0F, NOType.LOG, 5, 5);
                        if(IDToTypeMapping.get(idLef)==null)
                            addMapping(idLef, growSets[9], growthRates[9], dieSets[9], deathRates[9], 1.0F, 1.0F, NOType.LEAVES, 60, 10);
					}
				}
			}
		}
		option = new StringBuilder();
        Item it = null;
		for (Iterator itr = GameData.getItemRegistry().iterator();itr.hasNext(); it=(Item)itr.next()) {
			if (it instanceof ItemAxe) {
				option.append(GameData.getItemRegistry().getNameForObject(it)).append(",");
			}
		}
		ids = config.get(optionsCategory[1], "Lumberjack compatible items", option.toString(), "Separate item names with comma").getString().split(",");
		for (String param : ids) {
			if (param != null && !param.equals("")) {
				try {
                    it = GameData.getItemRegistry().getObject(param);
                    if(it!=null)
					    axes.add(it);
				} catch (Exception ignored) {
				}
			}
		}
        setBiomes();
		//Saving Forge recommended config file.
		if (config.hasChanged()) {
			config.save();
		}
		for (Block b : IDToFirePropagateMapping.keySet()) {
			Blocks.fire.setFireInfo(b, IDToFirePropagateMapping.get(b), IDToFireCatchMapping.get(b));
		}
		//Registering event listeners.
		MinecraftForge.EVENT_BUS.register(new ForgeEvents());
	}

    private void setBiomes() {
        for(BiomeGenBase biomeGenBase:BiomeGenBase.getBiomeGenArray()){
            if(biomeGenBase!=null){
                float tempt = config.getFloat(biomeGenBase.biomeName, "Biomes.Temperature", biomeGenBase.temperature, -1.0F, 2.0F, "");
                float rainf = config.getFloat(biomeGenBase.biomeName, "Biomes.Rainfall", biomeGenBase.rainfall, 0.0F, 1.0F, "");
                if(tempt>0.15F) {
                    if (tempt < 0.2F)
                        tempt = 0.2F;
                }else if(tempt>0.1F)
                        tempt = 0.1F;
                biomeGenBase.setTemperatureRainfall(tempt, rainf);
                boolean snow = config.getBoolean(biomeGenBase.biomeName, "Biomes.Snowing", biomeGenBase.func_150559_j(), "");
                boolean rain = ObfuscationReflectionHelper.getPrivateValue(BiomeGenBase.class, biomeGenBase, "enableRain", "field_76765_S");
                rain = config.getBoolean(biomeGenBase.biomeName, "Biomes.Raining", rain, "");
                ObfuscationReflectionHelper.setPrivateValue(BiomeGenBase.class, biomeGenBase, snow, "enableSnow", "field_76766_R");
                ObfuscationReflectionHelper.setPrivateValue(BiomeGenBase.class, biomeGenBase, rain, "enableRain", "field_76765_S");
            }
        }
    }

    /**
     * Sets all the menus and sub-options in ModOptionsAPI, if possible
     */
    private void tryUseMOAPI() {
        try {
            api = Class.forName("moapi.ModOptionsAPI");
            Method addMod = api.getMethod("addMod", String.class);
            //"addMod" is static, we don't need an instance
            Object option = addMod.invoke(null, "Nature Overhaul");
            Class<?> optionClass = addMod.getReturnType();
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
            for (String name: names) {
                addBoolean.invoke(subOption, name + " grow", true);
                addBoolean.invoke(subOption, name + " die", true);
                slidOption = addSlider.invoke(subOption, name + " growth rate", 0, 10000);
                setSliderValue.invoke(slidOption, 1200);
                slidOption = addSlider.invoke(subOption, name + " death rate", 0, 10000);
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
            slidOption = addSlider.invoke(fireOption, "Propagation range", 0, 20);
            setSliderValue.invoke(slidOption, 2);
            //Loads and saves values
            option = optionClass.getMethod("loadValues").invoke(option);
            option = optionClass.getMethod("saveValues").invoke(option);
            //We have saved the values, we can start to get them back
            getMOAPIValues(optionClass, subOption, lumberJackOption, miscOption, animalsOption, fireOption);
            //We successfully get all options !
            logger.info("NatureOverhaul found MOAPI and loaded all options correctly.");
        } catch (SecurityException s) {
            api = null;
        } catch (ClassNotFoundException c) {
            api = null;
            logger.info("NatureOverhaul couldn't use MOAPI, continuing with values in config file.");
        } catch (ReflectiveOperationException n) {
            api = null;
            logger.warn("NatureOverhaul failed to use MOAPI, please report to NO author:", n);
        }//Even if it fails, we can still rely on settings stored in Forge recommended config file.
    }

    @EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		config = new NOConfiguration(event);
		for (String name : optionsCategory) {
			config.addCustomCategoryComment(name, "The lower the rate, the faster the changes happen.");
		}
		config.addCustomCategoryComment(optionsCategory[2], "Plants are flower, deadbush, lilypad and tallgrass");
		config.getCategory(optionsCategory[13]).setRequiresMcRestart(true).setComment("");
        config.getCategory(optionsCategory[13]+".Spreading").setRequiresMcRestart(true).setComment("Chance of encouraging fire spread");
        config.getCategory(optionsCategory[13]+".Flammability").setRequiresMcRestart(true).setComment("Chance of catching fire");
        config.getCategory("Biomes").setRequiresWorldRestart(true);
        config.addCustomCategoryComment("Biomes.Temperature", "Modifying those values will affect growth and death rates if biome specific rates are enabled\nWarning, they can also change the biome color\nAvoid range 0.1 to 0.2 because of snow");
        config.addCustomCategoryComment("Biomes.Rainfall", "Modifying those values will affect growth and death rates if biome specific rates are enabled\nHigher for more humidity");
        config.addCustomCategoryComment("Biomes.Snowing", "Modifying those values will NOT affect growth and death rates\nWhen snowing is enabled, will replace rain and cancel lightning in most cases");
        config.addCustomCategoryComment("Biomes.Raining", "Modifying those values will NOT affect growth and death rates\nRemoving raining will cancel lightning in most cases");
        getBasicOptions();
        if(event.getSourceFile().getName().endsWith(".jar") && event.getSide().isClient()){
            try {
                Class.forName("mods.mud.ModUpdateDetector").getDeclaredMethod("registerMod", ModContainer.class, String.class, String.class).invoke(null,
                        FMLCommonHandler.instance().findContainerFor(this),
                        "https://raw.github.com/GotoLink/NatureOverhaul/master/update.xml",
                        "https://raw.github.com/GotoLink/NatureOverhaul/master/changelog.md"
                );
            } catch (Throwable ignored) {
            }
        }
	}

    @EventHandler
    public void onMessage(FMLInterModComms.IMCEvent event){
        for(FMLInterModComms.IMCMessage message : event.getMessages()){
            if("RegisterTree".equals(message.key) && message.isNBTMessage()){
                try {
                    TreeData data = new TreeData(message.getNBTValue());
                    if (data.isValid()) {
                        Block idSaplin = data.getBlock(TreeData.Component.SAPLING);
                        if (IDToTypeMapping.get(idSaplin) == null)
                            addMapping(idSaplin, growSets[0], 0, dieSets[0], deathRates[0], 0.8F, 0.8F, NOType.SAPLING);
                        Block idLo = data.getBlock(TreeData.Component.TRUNK);
                        if (IDToTypeMapping.get(idLo) == null)
                            addMapping(idLo, growSets[1], growthRates[1], dieSets[1], deathRates[1], 1.0F, 1.0F, NOType.LOG);
                        Block idLef = data.getBlock(TreeData.Component.LEAF);
                        if (IDToTypeMapping.get(idLef) == null)
                            addMapping(idLef, growSets[9], growthRates[9], dieSets[9], deathRates[9], 1.0F, 1.0F, NOType.LEAVES);
                        data.register();
                    }
                }catch (Throwable e){
                    logger.warn("Invalid NBT format", e);
                }
            }
        }
    }

    /**
     * Receive the change event from the configuration gui
     * Save the changes if needed
     * @param event the change
     */
    @SubscribeEvent
    public void onChange(ConfigChangedEvent.OnConfigChangedEvent event){
        if(event.modID.equals("natureoverhaul") && config.hasChanged()) {
            getBasicOptions();
            setBiomes();
            config.save();
            refreshBehaviors();
        }
    }

    private void getBasicOptions() {
        //Sapling options
        autoSapling = config.getBoolean(optionsCategory[0], "AutoSapling", true);
        despawnTimeSapling = config.getInt(optionsCategory[0], "Sapling item despawn rate", 6000);
        //Most growth/death stuff
        for (int i = 0; i < names.length; i++) {
            dieSets[i] = config.getBoolean(optionsCategory[i], names[i] + " Die", true);
            growSets[i] = config.getBoolean(optionsCategory[i], names[i] + " Grow", true);
            deathRates[i] = config.getInt(optionsCategory[i], names[i] + " Death Rate", 1200);
            growthRates[i] = config.getInt(optionsCategory[i], names[i] + " Growth Rate", 1200);
        }
        //Toggle between alternative time of growth for sapling
        growthType = GrowthType.valueOf(config.get(optionsCategory[0], "Sapling drops on", "Both", "Possible values are Neither,LeafGrowth,LeafDecay,Both").getString().toUpperCase(Locale.ENGLISH)).ordinal();
        //Toggle for lumberjack system on trees
        lumberjack = config.getBoolean(optionsCategory[1], "Enable lumberjack", true);
        killLeaves = config.getBoolean(optionsCategory[1], "Lumberjack kill leaves", true);
        //Apples don't have a dying system, because it is only an item
        growSets[names.length] = config.getBoolean(optionsCategory[9], "Apple Grows", true);
        growthRates[names.length] = config.getInt(optionsCategory[9], "Apple Growth Rate", 3000);
        //Force remove leaves after killing a tree, instead of letting Minecraft doing it
        decayLeaves = config.getBoolean(optionsCategory[9], "Enable leaves decay on tree death", true);
        //Toggle so Stone can turn into Mossy Cobblestone
        mossCorruptStone = config.getBoolean(optionsCategory[11], "Enable moss growing on stone", true);
        //Misc options
        useStarvingSystem = config.getBoolean(optionsCategory[names.length], "Enable starving system", true);
        biomeModifiedRate = config.getBoolean("Enable biome specific rates", "Biomes", true, "Should Biome Temperature and Rainfall values affect local growth and death rates");
        moddedBonemeal = config.getBoolean(optionsCategory[names.length], "Enable modded Bonemeal", true);
        customDimension = config.getBoolean(optionsCategory[names.length], "Enable custom dimensions", true);
        wildAnimalsBreed = config.getBoolean(optionsCategory[names.length], "Enable wild animals Breed", true);
        wildAnimalBreedRate = config.getInt("Wild animals breed rate", optionsCategory[names.length], 16000, 1, Integer.MAX_VALUE, "The lower the value, the higher the chance of breeding");
        wildAnimalDeathRate = config.getInt("Wild animals death rate", optionsCategory[names.length], 16000, 1, Integer.MAX_VALUE, "Mainly applies on animals that are unable to breed (old and alone)");
        autoFarming = config.getBoolean(optionsCategory[names.length], "Plant seeds on player drop", true);
    }

    /**
     * Core method. We make vanilla-like random ticks in loaded chunks.
     */
	@SubscribeEvent
	public void tickStart(TickEvent.WorldTickEvent event) {
        if(event.side.isServer()){
            if (event.phase == TickEvent.Phase.START && api != null) {
                tryRefreshWithMOAPIValues();
            }
            if (event.phase == TickEvent.Phase.END) {
                World world = event.world;
                if ((world.provider.dimensionId == 0 || (customDimension && world.provider.dimensionId != 1)) && !world.activeChunkSet.isEmpty()) {
                    Iterator<?> it = world.activeChunkSet.iterator();
                    while (it.hasNext()) {
                        ChunkCoordIntPair chunkIntPair = (ChunkCoordIntPair) it.next();
                        int k = chunkIntPair.chunkXPos * 16;
                        int l = chunkIntPair.chunkZPos * 16;
                        Chunk chunk = null;
                        if (world.getChunkProvider().chunkExists(chunkIntPair.chunkXPos, chunkIntPair.chunkZPos)) {
                            chunk = world.getChunkFromChunkCoords(chunkIntPair.chunkXPos, chunkIntPair.chunkZPos);
                        }
                        if (chunk != null && chunk.isChunkLoaded && chunk.isTerrainPopulated) {
                            int i2, k2, l2, i3;
                            Block j3;//Vanilla like random ticks for blocks
                            for (ExtendedBlockStorage blockStorage : chunk.getBlockStorageArray()) {
                                if (blockStorage != null && !blockStorage.isEmpty() && blockStorage.getNeedsRandomTick()) {
                                    for (int j2 = 0; j2 < 3; ++j2) {
                                        this.updateLCG = this.updateLCG * 3 + 1013904223;
                                        i2 = this.updateLCG >> 2;
                                        k2 = i2 & 15;
                                        l2 = i2 >> 8 & 15;
                                        i3 = i2 >> 16 & 15;
                                        j3 = blockStorage.getBlockByExtId(k2, i3, l2);
                                        if (j3!=Blocks.air && isRegistered(j3)) {
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
	}

    private void tryRefreshWithMOAPIValues() {
        try {
            Method getMod = api.getMethod("getModOptions", String.class);
            //"getMod" is static, we don't need an instance
            Object option = getMod.invoke(null, "Nature Overhaul");
            Class<?> optionClass = getMod.getReturnType();
            //To get a submenu
            Method getSubOption = optionClass.getMethod("getOption", String.class);
            Object subOption = getSubOption.invoke(option, "General");
            //Get "LumberJack" submenu
            Object lumberJackOption = getSubOption.invoke(option, "LumberJack");
            //Get "Misc" submenu
            Object miscOption = getSubOption.invoke(option, "Misc");
            //Get "Animals" submenu
            Object animalsOption = getSubOption.invoke(option, "Animals");
            //Get "Fire submenu
            Object fireOption = getSubOption.invoke(option, "Fire");
            //We can start to get the values back
            getMOAPIValues(optionClass, subOption, lumberJackOption, miscOption, animalsOption, fireOption);
        } catch (SecurityException s) {
            api = null;
        } catch (ReflectiveOperationException i) {
            api = null;
        }
        refreshBehaviors();
    }

    private void refreshBehaviors() {
        int index = -1;
        for (Block i : IDToTypeMapping.keySet()) {
            index = IDToTypeMapping.get(i).getIndex();
            if (index > -1) {
                if (growSets[index] != IDToGrowingMapping.get(i))
                    IDToGrowingMapping.put(i, growSets[index]);
                if (dieSets[index] != IDToDyingMapping.get(i))
                    IDToDyingMapping.put(i, dieSets[index]);
                IBehave behav = BehaviorManager.getBehavior(i);
                if (growthRates[index] != behav.getGrowthRate()) {
                    behav.setGrowthRate(growthRates[index]);
                }
                if (deathRates[index] != behav.getDeathRate()) {
                    behav.setDeathRate(deathRates[index]);
                }
            }
        }
    }

    /**
     *@return the complete path for the configuration file, or an empty string if there is none
     */
    public static String getConfigPath(){
        return INSTANCE.config!=null?INSTANCE.config.toString():"";
    }

    /**
     * @return the {@link ConfigElement}s from the configuration file, or null if there is none
     */
    @SideOnly(Side.CLIENT)
    public static List<IConfigElement> getConfigElements() {
        return INSTANCE.config!=null?INSTANCE.config.getElements():null;
    }

	/**
	 * The death general method.
     * Called by {@link #onUpdateTick(World, int, int, int, Block)} when conditions are fulfilled.
	 **/
	public static void death(World world, int i, int j, int k, Block id) {
		if (id instanceof IBlockDeath) {
			((IBlockDeath) id).death(world, i, j, k, id);
		} else {
			BehaviorManager.getBehavior(id).death(world, i, j, k, id);
		}
	}

	/**
	 * Special case for apples, since they don't have a corresponding block
	 * 
	 * @return apple growth probability at given coordinates
	 */
	public static float getAppleGrowthProb(World world, int i, int j, int k) {
		float freq = INSTANCE.growSets[names.length] ? INSTANCE.growthRates[names.length] * 1.5F : -1F;
		if (INSTANCE.biomeModifiedRate && freq > 0) {
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
	 * Get the growth probability.
     * Called by {@link #onUpdateTick(World, int, int, int, Block)}.
	 * 
	 * @return growth probability for given blockid and NOType at given
	 *         coordinates
	 */
	public static float getGrowthProb(World world, int i, int j, int k, Block id, NOType type) {
		float freq = getGrowthRate(id);
		if (INSTANCE.biomeModifiedRate && freq > 0 && type != NOType.NETHERSTALK) {
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

	public static Map<Block, NOType> getIDToTypeMapping() {
		return ImmutableMap.copyOf(IDToTypeMapping);
	}

	/**
	 * The general growing method.
     * Called by {@link #onUpdateTick(World, int, int, int, Block)} when conditions are fulfilled.
	 **/
	public static void grow(World world, int i, int j, int k, Block id) {
		if (id instanceof IGrowable) {
			((IGrowable) id).grow(world, i, j, k, id);
		} else {
			BehaviorManager.getBehavior(id).grow(world, i, j, k, id);
		}
	}

	/**
	 * Check if given block id is registered as growing
	 * 
	 * @param id
	 *            the block id to check
	 * @return true if block can grow
	 */
	public static boolean isGrowing(Block id) {
		return IDToGrowingMapping.get(id);
	}

	/**
	 * Check if given block id is registered as a log (may be part of a tree)
	 * 
	 * @param id
	 *            the block id to check
	 * @return true if block is a log
	 */
	public static boolean isLog(Block id, int meta) {
		return TreeData.getTree(id, meta, TreeData.Component.TRUNK)!=null || IDToTypeMapping.get(id) == NOType.MUSHROOMCAP;
	}

	public static boolean isRegistered(Block id) {
		return BehaviorManager.isRegistered(id);
	}

    public static boolean isAxe(Item id){return INSTANCE.axes.contains(id);}

	/**
	 * Registers all mappings simultaneously.
	 * 
	 * @param id
	 *            The id the block is registered with.
	 * @param isGrowing
	 *            Whether the block can call
	 *            {@link #grow(World, int, int, int, Block)} on tick.
	 * @param growthRate
	 *            How often the {@link #grow(World, int, int, int, Block)}
	 *            method will be called.
	 * @param isMortal
	 *            Whether the block can call
	 *            {@link #death(World, int, int, int, Block)} method on
	 *            tick.
	 * @param deathRate
	 *            How often the
	 *            {@link #death(World, int, int, int, Block)} method will
	 *            be called.
	 * @param optTemp
	 *            The optimal temperature parameter for the growth.
	 * @param optRain
	 *            The optimal humidity parameter for the growth.
	 * @param type
	 *            {@link NOType} Decides which growth and/or death to use, and
	 *            tolerance to temperature and humidity.
	 */
	public static void addMapping(Block id, boolean isGrowing, float growthRate, boolean isMortal, float deathRate, float optTemp, float optRain, NOType type) {
		IDToGrowingMapping.put(id, isGrowing);
		IDToDyingMapping.put(id, isMortal);
		IDToTypeMapping.put(id, type);
		BehaviorManager.setBehavior(id, BehaviorManager.getBehavior(type).setData(growthRate, deathRate, optRain, optTemp));
	}

	/**
	 * Registers all mappings simultaneously. Overloaded method with fire parameters.
	 * 
	 * @param fireCatch
	 *            Related to 3rd parameter in {@link
	 *            BlockFire#setFireInfo(Block,int,int)}.
	 * @param firePropagate
	 *            Related to 2nd parameter in {@link
	 *            BlockFire#setFireInfo(Block,int,int)}.
	 */
	public static void addMapping(Block id, boolean isGrowing, float growthRate, boolean isMortal, float deathRate, float optTemp, float optRain, NOType type, int fireCatch, int firePropagate) {
		addMapping(id, isGrowing, growthRate, isMortal, deathRate, optTemp, optRain, type);
		IDToFireCatchMapping.put(id, fireCatch);
		IDToFirePropagateMapping.put(id, firePropagate);
	}

	/**
	 * Helper reflection method for booleans
	 */
	public static boolean getBooleanFrom(Method meth, Object option, String name) throws ReflectiveOperationException {
		return Boolean.class.cast(meth.invoke(option, name)).booleanValue();
	}

	/**
	 * Get the death probability.
     * Called by {@link #onUpdateTick(World, int, int, int, Block)}.
	 * 
	 * @return Death probability for given blockid and NOType at given
	 *         coordinates
	 */
	private float getDeathProb(World world, int i, int j, int k, Block id, NOType type) {
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

	public static float getDeathRate(Block id) {
		if (id instanceof IBlockDeath) {
			return ((IBlockDeath) id).getDeathRate();
		} else {
			return BehaviorManager.getBehavior(id).getDeathRate();
		}
	}

	public static float getGrowthRate(Block id) {
		if (id instanceof IGrowable) {
			return ((IGrowable) id).getGrowthRate();
		} else {
			return BehaviorManager.getBehavior(id).getGrowthRate();
		}
	}

    /**
     * Helper reflection method for ints
     */
	public static int getIntFrom(Method meth, Object obj, String name) throws ReflectiveOperationException {
		return Integer.class.cast(meth.invoke(obj, name));
	}

	private void getMOAPIValues(Class<?> optionClass, Object subOption, Object lumberJackOption, Object miscOption, Object animalsOption, Object fireOption) throws SecurityException,
			ReflectiveOperationException {
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
		fireRange = getIntFrom(getSlider, fireOption, "Propagation range");
	}

	public static float getOptRain(Block id) {
		return BehaviorManager.getBehavior(id).getOptRain();
	}

	public static float getOptTemp(Block id) {
		return BehaviorManager.getBehavior(id).getOptTemp();
	}

	/**
	 * Called by {@link #onUpdateTick(World, int, int, int, Block)}.
     * Checks whether this block has died on this tick for any reason
	 * 
	 * @return True if plant has died
	 */
	private boolean hasDied(World world, int i, int j, int k, Block id) {
		if (id instanceof IBlockDeath) {
			return ((IBlockDeath) id).hasDied(world, i, j, k, id);
		} else {
			return BehaviorManager.getBehavior(id).hasDied(world, i, j, k, id);
		}
	}

	public static boolean isMortal(Block id) {
		return IDToDyingMapping.get(id);
	}

	/**
	 * Called from the world tick {@link #tickStart(TickEvent.WorldTickEvent)} with a
	 * {@link #isRegistered(Block)} block. Checks with {@link #isGrowing(Block)} or
	 * {@link #isMortal(Block)} booleans, and probabilities with
	 * {@link #getGrowthProb(World, int, int, int, Block, NOType)} or
	 * {@link #getDeathProb(World, int, int, int, Block, NOType)} then call
	 * {@link #grow(World, int, int, int, Block)} or
	 * {@link #death(World, int, int, int, Block)}.
	 */
	private void onUpdateTick(World world, int i, int j, int k, Block id) {
		NOType type = Utils.getType(id);
		if (isGrowing(id) && world.rand.nextFloat() < getGrowthProb(world, i, j, k, id, type)) {
			grow(world, i, j, k, id);
			return;
		}
		if (isMortal(id) && (hasDied(world, i, j, k, id) || world.rand.nextFloat() < getDeathProb(world, i, j, k, id, type))) {
			death(world, i, j, k, id);
		}
	}
}
