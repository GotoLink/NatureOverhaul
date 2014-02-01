package natureoverhaul.behaviors;

import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BehaviorMoss extends BehaviorDeathSwitch {
	@Override
	public Block getDeadBlock() {
		return Blocks.cobblestone;
	}

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 15;
	}

	@Override
	public void grow(World world, int i, int j, int k, Block id) {
		//Moss grows on both stone (or only cobblestone), changing only one block
		int scanSize = 1;
		Block iD;
		int coord[];
		for (int attempt = 0; attempt < 15; attempt++) {
			coord = Utils.findRandomNeighbour(i, j, k, scanSize);
			iD = world.func_147439_a(coord[0], coord[1], coord[2]);
			if ((NatureOverhaul.mossCorruptStone && iD == Blocks.stone) || iD == Blocks.cobblestone) {
				world.func_147449_b(coord[0], coord[1], coord[2], id);
				return;
			}
		}
	}
}
