package natureoverhaul;

//========
// START AUTOFOREST
//========
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.World;
//========
// END AUTOFOREST
//========

public class ItemSapling extends ItemPlantable {
    public ItemSapling(int i)
    {
        super(i);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    public int getMetadata(int i)
    {
        return i;
    }

    public int getIconFromDamage(int i)
    {
        return Block.sapling.getBlockTextureFromSideAndMetadata(0, i);
    }
	
	/**
	* Check if the item can be planted on top of the 
	* block with the idBelow at i, j, k
	*
	* @param	belowID		ID of block below
	* @param	age			Age of item
	* @return 	True when plantable
	*/
	public boolean plantable(World world, int i, int j, int k, int belowID, int age) {
		boolean saplingGrow =NatureOverhaul.autoSapling;
		return ((saplingGrow) && ((belowID == 2) || (belowID == 3)));
	}
	
	/**
	* Get the velocities of this item when it is created
	*
	* @param	baseSpeed	Base speed of item
	* @return	Array of speeds in format [x, y, z] velocities
	*/
	public float[] getVelocities(double baseSpeed) {
		float[] out = super.getVelocities(baseSpeed);
		
		out[1] = (float) (baseSpeed + (baseSpeed * Math.random() * 2));
		
		return out;
    }
    
	static {
		Item.itemsList[Block.sapling.blockID] = (new ItemSapling(Block.sapling.blockID - Block.blocksList.length)).setItemName("Sapling");
	}
}
