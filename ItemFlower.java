package natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/**
* Flower Item Representation for varied flowers.
*
* @author	Clinton Alexander
* @version	1.0.0.0
*/
public class ItemFlower extends ItemPlantable {
	
    public ItemFlower(int i)
    {
        super(i);
    }

    public int getPlacedBlockMetadata(int i)
    {
        return i;
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
		boolean flowerGrow 	=NatureOverhaul.flowerGrow; 
		return ((flowerGrow) && ((belowID == 2) || (belowID == 3)));
	}
	
	static {
		int offset = Block.blocksList.length;
		Item.itemsList[Block.plantYellow.blockID] = (new ItemFlower(Block.plantYellow.blockID - offset)).setItemName("Flower");
		Item.itemsList[Block.plantRed.blockID]	  = (new ItemFlower(Block.plantRed.blockID - offset)).setItemName("Flower");
	}
}
