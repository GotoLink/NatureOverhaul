package mods.natureoverhaul;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class AnimalEventHandler {
	 public int breedRate;
	public AnimalEventHandler(int wildAnimalBreedRate) {
		this.breedRate=wildAnimalBreedRate;
	}

	@ForgeSubscribe//Event for Wild Breeding
    public void onLivingUpdateEvent(LivingUpdateEvent event){
    	if(event.entityLiving instanceof EntityAnimal)
    	{
    		EntityAnimal ent =(EntityAnimal)event.entityLiving;
    		if(!ent.worldObj.isRemote && !ent.isChild() && !ent.isInLove()/*&& ent.breeding == 0*/) {
    			if(ent.worldObj.rand.nextInt(breedRate) == 0) {
    				ent.inLove = 600;
    			}
    		}
    	}
    }
}
