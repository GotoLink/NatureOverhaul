package natureoverhaul.events;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.entity.living.LivingEvent;

public class WildBreedingEvent extends LivingEvent {
	public static class Post extends WildBreedingEvent {
		/**
		 * Called after the child is born into the world
		 */
		public Post(EntityAnimal entity, EntityAnimal mate, EntityAgeable child) {
			super(entity, mate, child);
		}
	}

	@Cancelable
	public static class Pre extends WildBreedingEvent {
		/**
		 * Cancel to prevent any mating effects to happen, like age change,
		 * child spawn...
		 */
		public Pre(EntityAnimal entity, EntityAnimal mate, EntityAgeable child) {
			super(entity, mate, child);
		}
	}

	public final EntityAnimal animal;
	public final EntityAnimal partner;
	public EntityAgeable result;

	/**
	 * @param entity
	 *            the animal which searched to breed
	 * @param mate
	 *            the breeding partner found
	 * @param child
	 *            the default child they created
	 */
	public WildBreedingEvent(EntityAnimal entity, EntityAnimal mate, EntityAgeable child) {
		super(entity);
		this.animal = entity;
		this.partner = mate;
		this.result = child;
	}

	public static EntityAgeable getResult(Pre event) {
		if (!MinecraftForge.EVENT_BUS.post(event)) {
			return event.result;
		} else {
			return null;
		}
	}
}
