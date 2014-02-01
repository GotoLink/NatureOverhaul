package natureoverhaul.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import natureoverhaul.NOType;
import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.item.ItemExpireEvent;

/**
 * Event for AutoSapling, from Clinton Alexander idea.
 * 
 * @author Olivier
 */
public class AutoSaplingEventHandler {
	private boolean auto;

	public AutoSaplingEventHandler(boolean autoSapling) {
		this.auto = autoSapling;
	}

	@SubscribeEvent
	public void onSaplingItemDead(ItemExpireEvent event) {
		if (auto) {
			EntityItem ent = event.entityItem;
			if (ent.motionX < 0.001 && ent.motionZ < 0.001) {
				ItemStack item = ent.getEntityItem();
                if(item!=null){
                    Block id = Block.func_149634_a(item.getItem());
                    int x = MathHelper.floor_double(ent.posX);
                    int y = MathHelper.floor_double(ent.posY);
                    int z = MathHelper.floor_double(ent.posZ);
                    if (NatureOverhaul.isRegistered(id) && Utils.getType(id) == NOType.SAPLING && id.func_149742_c(ent.worldObj, x, y, z)) {
                        ent.worldObj.func_147465_d(x, y, z, id, item.getItemDamage(), 3);
                    }
                }
			}
		}
	}

	public void set(boolean autoSapling) {
		this.auto = autoSapling;
	}
}
