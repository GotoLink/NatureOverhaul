package assets.natureoverhaul;

import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
/**
 * Event for Lumberjack system, from Clinton Alexander idea.
 * @author Olivier
 *
 */
public class PlayerEventHandler {
	private boolean leafKill,lumber;
	
	public PlayerEventHandler(boolean lumberjack, boolean killLeaves) {
		this.leafKill=killLeaves;
		this.lumber=lumberjack;
	}

	@ForgeSubscribe
    public void onPlayerInteracting(PlayerInteractEvent event){
    	if(lumber && event.action==Action.LEFT_CLICK_BLOCK){
    		ItemStack itemstack = event.entityPlayer.getCurrentEquippedItem();
    		if(itemstack != null) {
    			Item it = itemstack.getItem();
    			//Check for an axe in player hand
    			int i=event.x;
				int j=event.y;
				int k=event.z;
    			if(it!=null && it instanceof ItemAxe && !it.onBlockStartBreak(itemstack, i, j, k, event.entityPlayer)){
    				int id=event.entityPlayer.worldObj.getBlockId(i, j, k);
    				//Check for a registered log block
    				if(NatureOverhaul.instance.isLog(id) ){
    					World world=event.entityPlayer.worldObj;
    					
    					if( TreeUtils.isTree(world, i, j, k, Utils.getType(id), true) )
    					{
    						int meta = world.getBlockMetadata(i, j, k);
    						// Damage axe compared to the number of blocks found
					  		int damage = TreeUtils.killTree(world, i, j, k, id, leafKill);
        					itemstack.damageItem(damage - 1, event.entityPlayer);
        					if(itemstack.stackSize <= 0) 
        						event.entityPlayer.destroyCurrentEquippedItem();
        					//Drop logs
        					Utils.emitItem(world, i, j, k, new ItemStack(id, damage, meta));
    					}
    				}
    			}
    		}
    	}	
    }

	public void set(boolean lumberjack, boolean killLeaves) {
		this.leafKill=killLeaves;
		this.lumber=lumberjack;
	}
}
