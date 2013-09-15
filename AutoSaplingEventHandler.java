package assets.natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
/**
 * Event for AutoSapling, from Clinton Alexander idea.
 * @author Olivier
 *
 */
public class AutoSaplingEventHandler {
	private boolean auto;
	public AutoSaplingEventHandler(Boolean autoSapling) {
		this.auto=autoSapling;
	}

	@ForgeSubscribe
    public void onSaplingItemDead(ItemExpireEvent event){	
    	if(auto){
			EntityItem ent=event.entityItem;
			if(ent.motionX<0.001 && ent.motionZ<0.001){
				ItemStack item = ent.getEntityItem();
				int id=item.itemID;
				int x=MathHelper.floor_double(ent.posX);
				int y=MathHelper.floor_double(ent.posY);
				int z=MathHelper.floor_double(ent.posZ);
				if(NatureOverhaul.instance.isRegistered(id) && Utils.getType(id)==NOType.SAPLING
						&& Block.blocksList[id].canPlaceBlockAt(ent.worldObj, x, y, z)){
					ent.worldObj.setBlock(x, y, z, id, item.getItemDamage(), 3);
				}
			}
    	}
    }

	public void set(Boolean autoSapling) {
		this.auto=autoSapling;
	}
}
