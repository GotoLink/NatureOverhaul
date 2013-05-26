package mods.natureoverhaul;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAITaskEntry;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
/**
 * Event for wild animals breeding, from Clinton Alexander idea.
 * @author Olivier
 *
 */
public class AnimalEventHandler {
	private int breedRate,time;
	private boolean breed;
	public AnimalEventHandler(boolean wildAnimalsBreed, int wildAnimalBreedRate) {
		this.breedRate=wildAnimalBreedRate;
		this.breed=wildAnimalsBreed;
		this.time=this.breedRate;
	}

	@ForgeSubscribe
    public void onLivingUpdateEvent(LivingUpdateEvent event){
		if(breed && time>0)
			time--;
    	if(breed && time==0 && event.entityLiving instanceof EntityAnimal)
    	{
    		time = breedRate;
    		EntityAnimal ent =(EntityAnimal)event.entityLiving;
    		if(!ent.worldObj.isRemote && !ent.isChild()) 
    		{
					EntityAnimal mate=getNearbyMate(ent);
					if(mate!=null)
					{
						EntityAgeable entityageable = ent.createChild(mate);
				        if (entityageable != null)
				        {
				            ent.setGrowingAge(6000);
				            mate.setGrowingAge(6000);
				            entityageable.setGrowingAge(-24000);
				            entityageable.setLocationAndAngles(ent.posX, ent.posY, ent.posZ, 0.0F, 0.0F);
				            ent.worldObj.spawnEntityInWorld(entityageable);
				            Random random = ent.getRNG();
				            for (int i = 0; i < 7; ++i)
				            {
				                double d0 = random.nextGaussian() * 0.02D;
				                double d1 = random.nextGaussian() * 0.02D;
				                double d2 = random.nextGaussian() * 0.02D;
				                ent.worldObj.spawnParticle("heart", ent.posX + (double)(random.nextFloat() * ent.width * 2.0F) - (double)ent.width, ent.posY + 0.5D + (double)(random.nextFloat() * ent.height), ent.posZ + (double)(random.nextFloat() * ent.width * 2.0F) - (double)ent.width, d0, d1, d2);
				            }
				         }
					}
                }
    		}
    	}
    private static EntityAnimal getNearbyMate(EntityAnimal ent) {
    	double d0 = 8.0D;
        List list = ent.worldObj.getEntitiesWithinAABB(ent.getClass(), ent.boundingBox.expand(d0, d0, d0));
        d0 = Double.MAX_VALUE;
        EntityAnimal entityanimal = null;
        Iterator iterator = list.iterator();
        while (iterator.hasNext())
        {
            EntityAnimal entityanimal1 = (EntityAnimal)iterator.next();
            if (ent.getClass()==entityanimal1.getClass() && ent!=entityanimal1 && ent.getDistanceSqToEntity(entityanimal1) < d0)
            {
                entityanimal = entityanimal1;
                d0 = ent.getDistanceSqToEntity(entityanimal1);
            }
        }
        return entityanimal;
	}

	public void set(boolean wildAnimalsBreed, int wildAnimalBreedRate) {
		this.breedRate=wildAnimalBreedRate;
		this.breed=wildAnimalsBreed;
	}
}
