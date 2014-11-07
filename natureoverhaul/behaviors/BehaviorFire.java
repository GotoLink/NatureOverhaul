package natureoverhaul.behaviors;

import static net.minecraftforge.common.util.ForgeDirection.*;

import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BehaviorFire extends BehaviorDeathDisappear {
	protected int limit = 15;
    public BehaviorFire(){
        super(null, 30);
    }

	@Override
	public void death(World world, int i, int j, int k, Block id) {
		super.death(world, i, j, k, id);
		if (isBurnableGround(world.getBlock(i, j - 1, k))) {
			world.setBlock(i, j - 1, k, getBurnedGround());
		}
	}

	public Block getBurnedGround() {
		return Blocks.dirt;
	}

	public int getRange() {
		return NatureOverhaul.INSTANCE.fireRange;
	}

	@Override
	public void grow(World world, int i, int j, int k, Block block) {
		if (block instanceof BlockFire) {
			int l = world.getBlockMetadata(i, j, k);
			if (l > 0) {
				world.setBlockMetadataWithNotify(i, j, k, l - 1, 4);
			}
			if (isBurnableGround(world.getBlock(i, j - 1, k))) {
				world.setBlock(i, j - 1, k, getBurnedGround());
			}
		}
		int[] neighbour;
		Block nId;
        int tries = 0;
		while (tries < limit) {
			neighbour = Utils.findRandomNeighbour(i, j, k, getRange());
			nId = world.getBlock(neighbour[0], neighbour[1], neighbour[2]);
			if (canNeighborBurn(world, neighbour[0], neighbour[1], neighbour[2], nId)) {
				world.setBlock(neighbour[0], neighbour[1], neighbour[2], block, 0, 3);
			}
			tries++;
		}
	}

	public boolean isBurnableGround(Block id) {
		return id == Blocks.grass;
	}

	private boolean canNeighborBurn(World world, int x, int y, int z, Block block) {
		return block.isAir(world, x, y, z) || block.isFlammable(world, x, y, z, WEST) || block.isFlammable(world, x, y, z, EAST) || block.isFlammable(world, x, y, z, UP)
				|| block.isFlammable(world, x, y, z, DOWN) || block.isFlammable(world, x, y, z, SOUTH) || block.isFlammable(world, x, y, z, NORTH);
	}
}
