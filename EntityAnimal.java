package mods.natureoverhaul;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityAnimal extends EntityCreature
{
    private int inLove;
    private int breeding;

    public EntityAnimal(World world)
    {
        super(world);
        breeding = 0;
    }

    public void onLivingUpdate()
    {   
		//========
		// BEGIN NATURE OVERHAUL
		//========
		if(!worldObj.isRemote && !isChild() && wildBreeding && (inLove == 0) && (breeding == 0)) {
			if(worldObj.rand.nextInt(breedRate) == 0) {
				inLove = 600;
			}
		}
		//========
		// END NATURE OVERHAUL
		//========
    }
	
	//========
	// BEGIN NATURE OVERHAUL
	//========
	
	// For the sake of brevity, import the options to this object
	public static final Boolean wildBreeding = NatureOverhaul.wildAnimalsBreed;
	public static final int breedRate = NatureOverhaul.wildAnimalBreedRate;
	
	//========
	// END NATURE OVERHAUL
	//========
}
