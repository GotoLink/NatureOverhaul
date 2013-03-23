package natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.ModLoader;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityItem extends Entity
{
    public ItemStack item;
    public int age;
    public int delayBeforeCanPickup;
    private int health;
    public float hoverStart;
    public int lifespan = 6000;
	//========
	// BEGIN NATURE OVERHAUL
	//========
    public EntityItem(World world, double d, double d1, double d2,
            ItemStack itemstack) {
		this(world, d, d1, d2, itemstack, false);
		this.lifespan = (itemstack.getItem() == null ? 6000 : itemstack.getItem().getEntityLifespan(itemstack, world));
		   
    }
	
	/**
	* Constructor
	*
	* @param	grown	If true, the item will fly further than usual
	*/
    public EntityItem(World world, double d, double d1, double d2, 
            ItemStack itemstack, boolean grown) {
        super(world);
        age = 0;
        health = 5;
        hoverStart = (float)(Math.random() * Math.PI * 2D);
        setSize(0.25F, 0.25F);
        yOffset = height / 2.0F;
        setPosition(d, d1, d2);
        item = itemstack;
        rotationYaw = (float)(Math.random() * 360D);
        motionX = (float)(Math.random() * 0.2D - 0.1D);
        motionY = 0.2D;
        motionZ = (float)(Math.random() * 0.2D - 0.1D);
		
	}
	//======== 
	// END NATURE OVERHAUL
	//========

    public void onUpdate()
    {
        super.onUpdate();
        if (delayBeforeCanPickup > 0)
        {
            delayBeforeCanPickup--;
        }
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (worldObj.getBlockMaterial(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)) == Material.lava)
        {
            motionY = 0.20000000298023221D;
            motionX = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
            motionZ = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
            worldObj.playSoundAtEntity(this, "random.fizz", 0.4F, 2.0F + rand.nextFloat() * 0.4F);
        }
        pushOutOfBlocks(posX, (boundingBox.minY + boundingBox.maxY) / 2D, posZ);
        moveEntity(motionX, motionY, motionZ);
        float f = 0.98F;
        if (onGround)
        {
            f = 0.5880001F;
            int i = worldObj.getBlockId(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY) - 1, MathHelper.floor_double(posZ));
            if (i > 0)
            {
                f = Block.blocksList[i].slipperiness * 0.98F;
            }
			//========
			// BEGIN NATURE OVERHAUL
			//========
			if(!ModLoader.getMinecraftInstance().theWorld.isRemote) {
				attemptPlant(i);
			}
			//========
			// END NATURE OVERHAUL
			//========
        }
        motionX *= f;
        motionY *= 0.98000001907348633D;
        motionZ *= f;
        if (onGround)
        {
            motionY *= -0.5D;
        }       
        age++;
        if (age >= 6000)
        {
            setDead();
        }
		//setEntityDead();
    }
	//========
	// BEGIN NATURE OVERHAUL
	//========
	
	/**
	* Attempt to plant the current plant item
	*
	* @param	world		World object
	* @param	belowID		ID of block below
	*/
	private void attemptPlant(int belowID) {
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		// Get block id the entity is occupying
		int curBlockID = worldObj.getBlockId(i, j, k);
		
		// Api-able plantable interface
		if((age > 1200) && ((curBlockID == 0) || (curBlockID == Block.snow.blockID) 
							|| (curBlockID == Block.tallGrass.blockID)
							|| (curBlockID == Block.slowSand.blockID)) 
			&& (Item.itemsList[item.itemID] instanceof IPlantable)) {
			IPlantable pItem = (IPlantable) Item.itemsList[item.itemID];
			if(pItem.plantable(worldObj, i, j, k, belowID, age)) {
				pItem.plant(worldObj, i, j, k, item.getItemDamage());
				setDead();
			}
		}
	}
	
	//========
	// END NATURE OVERHAUL
	//========  
}
