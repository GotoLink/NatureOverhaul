package assets.natureoverhaul.behaviors;

import assets.natureoverhaul.NatureOverhaul;
import assets.natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class BehaviorMoss extends BehaviorDeathSwitch {
	@Override
	public int getDeadBlockId() {
		return Block.cobblestone.blockID;
	}

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 15;
	}

	@Override
	public void grow(World world, int i, int j, int k, int id) {
		//Moss grows on both stone (or only cobblestone), changing only one block
		int scanSize = 1;
		int iD;
		int coord[];
		for (int attempt = 0; attempt < 15; attempt++) {
			coord = Utils.findRandomNeighbour(i, j, k, scanSize);
			iD = world.getBlockId(coord[0], coord[1], coord[2]);
			if ((NatureOverhaul.mossCorruptStone && iD == Block.stone.blockID) || iD == Block.cobblestone.blockID) {
				world.setBlock(coord[0], coord[1], coord[2], id);
				return;
			}
		}
	}
}
