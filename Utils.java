package mods.natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Utils {

	/**
	* Calculate an optimal distance coefficient. This is for cases
	* where a larger number is desired the further you get from
	* the optimal value. The minimum is 1, so multiplying any number "r"
	* by the result of this operation will result in "r" if r is equal to "opt".
	* If r is extremely far from opt, the coefficient will be extremely large
	*
	* @param	rain		Current value
	* @param	opt		Optimal value
	* @param	tol		tolerance (lower = Higher probability)
	* @return The modifier. Output always >= 1, where 1 is "just as likely" and
	* 		higher is "less likely"
	*/
	public static float getOptValueMult(float rain, float opt, float tol) {	
		return tol * (float) Math.pow(opt - rain, 2) + 1;
	}

	public static NOType getType(int id){
		return NatureOverhaul.instance.getIDToTypeMapping().get(Integer.valueOf(id));
	}
	/**
	* Check if we have nearby block of corresponding id within radius
	*
	* @param	radius	Radius to check in
	* @return	True if at least one block nearby
	*/
	public static boolean hasNearbyBlock(World world, int i, int j, int k, int id, int radius) {
		for(int x = -radius; x <= radius; x++) {
			for(int y = -radius; y <= radius; y++) {
				for(int z = -radius; z <= radius; z++) {
					if(world.getBlockId(i + x, j + y, k + z) == id) {
						return true;
					}
				}
			}
		}	
		return false;
	}
	
	/**
	* Emit a specific item
	*
	* @param item	Item to emit
	*/
	public static void emitItem(World world, int i, int j, int k, ItemStack item) {
		if(!world.isRemote)
		{
			EntityItem entityitem = new EntityItem(world, i, j, k, item);
			world.spawnEntityInWorld(entityitem);
		}
	}
	
	public String toString(){
		return "Nature Overhaul Utility Class";
	}
}
