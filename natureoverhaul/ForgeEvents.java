package natureoverhaul;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import natureoverhaul.events.FarmingEvent;
import natureoverhaul.events.LumberJackEvent;
import natureoverhaul.events.WildBreedingEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;
import java.util.Random;

/**
 * Created by Olivier on 28/08/2014.
 * All the event listeners needed for various parts of the mod
 */
public final class ForgeEvents {
    public static final ForgeEvents INSTANCE = new ForgeEvents();
    private ForgeEvents(){}

    /**
     * Event for wild animals breeding, from Clinton Alexander idea.
     */
    @SubscribeEvent
    public void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event) {
        if (NatureOverhaul.INSTANCE.wildAnimalsBreed && event.entityLiving instanceof EntityAnimal) {
            EntityAnimal ent = (EntityAnimal) event.entityLiving;
            if (!ent.worldObj.isRemote && !ent.isNoDespawnRequired() && ent.getGrowingAge() == 0 && !ent.isInLove() && !ent.hasCustomName()) {
                EntityAnimal mate = getNearbyMate(ent);
                if (mate != null && ent.getRNG().nextFloat() < 1 / NatureOverhaul.INSTANCE.wildAnimalBreedRate) {
                    EntityAgeable entityageable = ent.createChild(mate);//create the baby
                    entityageable = WildBreedingEvent.getResult(new WildBreedingEvent.Pre(ent, mate, entityageable));
                    if (entityageable != null) {
                        ent.setGrowingAge(6000);//reset parents mating counter
                        mate.setGrowingAge(6000);
                        entityageable.setGrowingAge(-24000);//set child aging counter
                        entityageable.setLocationAndAngles(ent.posX, ent.posY, ent.posZ, 0.0F, 0.0F);
                        ent.worldObj.spawnEntityInWorld(entityageable);
                        MinecraftForge.EVENT_BUS.post(new WildBreedingEvent.Post(ent, mate, entityageable));
                        Random random = ent.getRNG();
                        for (int i = 0; i < 7; ++i) {
                            double d0 = random.nextGaussian() * 0.02D;
                            double d1 = random.nextGaussian() * 0.02D;
                            double d2 = random.nextGaussian() * 0.02D;
                            ent.worldObj.spawnParticle(EnumParticleTypes.HEART, ent.posX + random.nextFloat() * ent.width * 2.0F - ent.width, ent.posY + 0.5D + random.nextFloat() * ent.height,
                                    ent.posZ + random.nextFloat() * ent.width * 2.0F - ent.width, d0, d1, d2);
                        }
                    }
                } else if (ent.getRNG().nextFloat() < 1 / NatureOverhaul.INSTANCE.wildAnimalDeathRate) {//low chance of dying
                    ent.setDead();
                }
            }
        }
    }

    /**
     * Helper method to search for compatible mate
     * @param ent to search a partner for
     * @return the mating partner or null if none is found
     */
    @SuppressWarnings("unchecked")
    public static EntityAnimal getNearbyMate(EntityAnimal ent) {
        double d0 = 8.0D;//search entities around
        List<EntityAnimal> list = ent.worldObj.getEntitiesWithinAABB(ent.getClass(), ent.getEntityBoundingBox().expand(d0, d0, d0));
        d0 = Double.MAX_VALUE;
        EntityAnimal entityanimal = null;
        for (EntityAnimal entityanimal1 : list) {
            if (ent.getClass() == entityanimal1.getClass() && ent != entityanimal1 && ent.getDistanceSqToEntity(entityanimal1) < d0) {
                entityanimal = entityanimal1;//get closest entity , but don't crossbread
                d0 = ent.getDistanceSqToEntity(entityanimal1);
            }
        }
        return entityanimal;
    }

    /**
     * Auto plant dropped seeds on fertile grounds
     */
    @SubscribeEvent
    public void onSeedsDropped(ItemTossEvent event) {
        if (NatureOverhaul.INSTANCE.autoFarming) {
            Item item = event.entityItem.getEntityItem().getItem();
            if (item instanceof IPlantable) {
                World world = event.player.worldObj;
                BlockPos info;
                IBlockState id;
                for (int tries = 0; tries < 40; tries++) {
                    info = Utils.findRandomNeighbour(new BlockPos(event.player).down(), 3);
                    if (world.getBlockState(info).getBlock() == (item == Items.nether_wart ? Blocks.soul_sand : Blocks.farmland)
                            && world.isAirBlock(info.up())) {
                        if (!MinecraftForge.EVENT_BUS.post(new FarmingEvent(event.player, (IPlantable) item, info))) {
                            id = ((IPlantable) item).getPlant(world, info);
                            world.setBlockState(info.up(), id);
                            event.setCanceled(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Event for AutoSapling, from Clinton Alexander idea.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSaplingItemDead(ItemExpireEvent event) {
        if (NatureOverhaul.INSTANCE.autoSapling) {
            EntityItem ent = event.entityItem;
            if (ent.motionX < 0.001 && ent.motionZ < 0.001) {
                ItemStack item = ent.getEntityItem();
                if(item!=null && item.stackSize > 0 && item.getItem() instanceof ItemBlock){
                    Block id = ((ItemBlock) item.getItem()).block;
                    if (NatureOverhaul.isRegistered(id) && Utils.getType(id.getDefaultState()) == NOType.SAPLING){
                        BlockPos pos = new BlockPos(ent);
                        if(ent.worldObj.canBlockBePlaced(id, pos, false, EnumFacing.UP, null, item)) {
                            IBlockState state = id.getStateFromMeta(item.getItem().getMetadata(item.getMetadata()));
                            if(ent.worldObj.setBlockState(pos, state)){
                                item.stackSize--;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Modded bonemeal from Clinton Alexander idea.
     */
    @SubscribeEvent
    public void onBoneMealUse(BonemealEvent event) {
        if (NatureOverhaul.INSTANCE.moddedBonemeal && applyBonemeal(event.world, event.pos, event.block)) {
            event.setResult(Event.Result.ALLOW);//BoneMeal is consumed, but doesn't act vanilla
        }
    }

    /**
     * Apply bonemeal to the location clicked
     * @return true if item is applied
     */
    private boolean applyBonemeal(World world, BlockPos pos, IBlockState state) {
        Block id = state.getBlock();
        if (NatureOverhaul.isRegistered(id) && NatureOverhaul.isGrowing(id)) {
            if (Utils.getType(state) != NOType.GRASS) {
                if(!world.isRemote){
                    NatureOverhaul.grow(world, pos, state);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Event listener for Lumberjack system, from Clinton Alexander idea.
     */
    @SubscribeEvent
    public void onPlayerHarvesting(BlockEvent.HarvestDropsEvent event) {
        if (NatureOverhaul.INSTANCE.lumberjack && event.harvester != null) {
            ItemStack itemstack = event.harvester.getCurrentEquippedItem();
            if (itemstack != null) {
                //Check for an axe in player hand
                if (NatureOverhaul.isAxe(itemstack.getItem())) {
                    IBlockState id = event.state;
                    //Check for a registered log block
                    if (NatureOverhaul.isLog(id) && !MinecraftForge.EVENT_BUS.post(new LumberJackEvent(event, itemstack))) {
                        if (TreeUtils.isTree(event.world, event.pos, Utils.getType(id), true)) {
                            //Damage axe compared to the number of blocks found
                            int damage = TreeUtils.killTree(event.world, event.pos, id, NatureOverhaul.INSTANCE.killLeaves);
                            itemstack.damageItem(damage - 1, event.harvester);
                            if (itemstack.stackSize <= 0)
                                event.harvester.destroyCurrentEquippedItem();
                            //Drop logs
                            event.drops.add(new ItemStack(id.getBlock(), damage, id.getBlock().damageDropped(id)));
                        }
                    }
                }
            }
        }
    }

    /*
     * Sapling doesn't grow vanilla with even growType
     */
    @SubscribeEvent
    public void onGrowingSapling(SaplingGrowTreeEvent event) {
        if (NatureOverhaul.INSTANCE.growthType % 2 == 0) {
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * Custom life span for sapling items
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSaplingSpawn(EntityJoinWorldEvent event){
        if(event.entity instanceof EntityItem){
            ItemStack stack = ((EntityItem) event.entity).getEntityItem();
            if(stack!=null && stack.getItem()!=null){
                int life = stack.getItem().getEntityLifespan(stack, event.world);
                if(life!=NatureOverhaul.INSTANCE.despawnTimeSapling && Block.getBlockFromItem(stack.getItem()) instanceof BlockSapling) {
                    ((EntityItem) event.entity).lifespan = NatureOverhaul.INSTANCE.despawnTimeSapling;
                }
            }
        }
    }
}
