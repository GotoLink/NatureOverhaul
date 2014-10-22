package natureoverhaul;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
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

import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by Olivier on 28/08/2014.
 */
public class ForgeEvents {
    /**
     * Event for wild animals breeding, from Clinton Alexander idea.
     */
    @SubscribeEvent
    public void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event) {
        if (NatureOverhaul.INSTANCE.wildAnimalsBreed && event.entityLiving instanceof EntityAnimal) {
            EntityAnimal ent = (EntityAnimal) event.entityLiving;
            if (!ent.worldObj.isRemote && !ent.isNoDespawnRequired() && ent.getGrowingAge() == 0 && !ent.isInLove() && !ent.hasCustomNameTag()) {
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
                            ent.worldObj.spawnParticle("heart", ent.posX + random.nextFloat() * ent.width * 2.0F - ent.width, ent.posY + 0.5D + random.nextFloat() * ent.height,
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
     * @param ent
     * @return the mating partner or null if none is found
     */
    public static EntityAnimal getNearbyMate(EntityAnimal ent) {
        double d0 = 8.0D;//search entities around
        List<?> list = ent.worldObj.getEntitiesWithinAABB(ent.getClass(), ent.boundingBox.expand(d0, d0, d0));
        d0 = Double.MAX_VALUE;
        EntityAnimal entityanimal = null;
        Iterator<?> iterator = list.iterator();
        while (iterator.hasNext()) {
            EntityAnimal entityanimal1 = (EntityAnimal) iterator.next();
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
                int[] info;
                Block id;
                int meta;
                for (int tries = 0; tries < 40; tries++) {
                    info = Utils.findRandomNeighbour((int) event.player.posX, (int) event.player.posY - 1, (int) event.player.posZ, 3);
                    if (world.getBlock(info[0], info[1], info[2]) == (item == Items.nether_wart ? Blocks.soul_sand : Blocks.farmland)
                            && world.isAirBlock(info[0], info[1] + 1, info[2])) {
                        if (!MinecraftForge.EVENT_BUS.post(new FarmingEvent(event.player, (IPlantable) item, info[0], info[1], info[2]))) {
                            id = ((IPlantable) item).getPlant(world, info[0], info[1], info[2]);
                            meta = ((IPlantable) item).getPlantMetadata(world, info[0], info[1], info[2]);
                            world.setBlock(info[0], info[1] + 1, info[2], id, meta, 3);
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
    @SubscribeEvent
    public void onSaplingItemDead(ItemExpireEvent event) {
        if (NatureOverhaul.INSTANCE.autoSapling) {
            EntityItem ent = event.entityItem;
            if (ent.motionX < 0.001 && ent.motionZ < 0.001) {
                ItemStack item = ent.getEntityItem();
                if(item!=null && item.getItem() instanceof ItemBlock){
                    Block id = Block.getBlockFromItem(item.getItem());
                    int x = MathHelper.floor_double(ent.posX);
                    int y = MathHelper.floor_double(ent.posY);
                    int z = MathHelper.floor_double(ent.posZ);
                    if (NatureOverhaul.isRegistered(id) && Utils.getType(id) == NOType.SAPLING && id.canPlaceBlockAt(ent.worldObj, x, y, z)) {
                        ent.worldObj.setBlock(x, y, z, id, item.getItemDamage(), 3);
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
        if (NatureOverhaul.INSTANCE.moddedBonemeal) {
            if (applyBonemeal(event.world, event.x, event.y, event.z, event.block)) {
                event.setResult(Event.Result.ALLOW);//BoneMeal is consumed, but doesn't act vanilla
            }
        }
    }

    /**
     * Apply bonemeal to the location clicked
     * @return true if item is applied
     */
    private boolean applyBonemeal(World world, int i, int j, int k, Block id) {
        if (NatureOverhaul.isRegistered(id) && NatureOverhaul.isGrowing(id)) {
            if (Utils.getType(id) != NOType.GRASS) {
                if(!world.isRemote){
                    NatureOverhaul.grow(world, i, j, k, id);
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
                    Block id = event.block;
                    //Check for a registered log block
                    if (NatureOverhaul.isLog(id, event.blockMetadata) && !MinecraftForge.EVENT_BUS.post(new LumberJackEvent(event, itemstack))) {
                        if (TreeUtils.isTree(event.world, event.x, event.y, event.z, Utils.getType(id), true)) {
                            //Damage axe compared to the number of blocks found
                            int damage = TreeUtils.killTree(event.world, event.x, event.y, event.z, id, NatureOverhaul.INSTANCE.killLeaves);
                            itemstack.damageItem(damage - 1, event.harvester);
                            if (itemstack.stackSize <= 0)
                                event.harvester.destroyCurrentEquippedItem();
                            //Drop logs
                            Utils.emitItem(event.world, event.x, event.y, event.z, new ItemStack(id, damage, event.blockMetadata));
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
