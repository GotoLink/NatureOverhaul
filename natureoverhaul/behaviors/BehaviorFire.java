package natureoverhaul.behaviors;

import static net.minecraftforge.common.ForgeDirection.*;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.world.World;

public class BehaviorFire extends BehaviorDeathDisappear {
	protected int fireRange = 2;
	protected int limit = 15;

	@Override
	public void death(World world, int i, int j, int k, int id) {
		super.death(world, i, j, k, id);
		if (isBurnableGround(world.getBlockId(i, j - 1, k))) {
			world.setBlock(i, j - 1, k, getBurnedGround());
		}
	}

	public int getBurnedGround() {
		return Block.dirt.blockID;
	}

	@Override
	public int getMaxNeighbour(World world, int i, int j, int k) {
		return 30;
	}

	public int getRange() {
		return this.fireRange;
	}

	@Override
	public void grow(World world, int i, int j, int k, int id) {
		Block block = Block.blocksList[id];
		if (block instanceof BlockFire) {
			int l = world.getBlockMetadata(i, j, k);
			if (l > 0) {
				world.setBlockMetadataWithNotify(i, j, k, l - 1, 4);
			}
			if (isBurnableGround(world.getBlockId(i, j - 1, k))) {
				world.setBlock(i, j - 1, k, getBurnedGround());
			}
		}
		int[] neighbour = null;
		int nId, tries = 0;
		while (tries < limit) {
			neighbour = Utils.findRandomNeighbour(i, j, k, fireRange);
			nId = world.getBlockId(neighbour[0], neighbour[1], neighbour[2]);
			if (canNeighborBurn(world, neighbour[0], neighbour[1], neighbour[2], nId)) {
				world.setBlock(neighbour[0], neighbour[1], neighbour[2], id, 0, 3);
			}
			tries++;
		}
	}

	public boolean isBurnableGround(int id) {
		return id == Block.grass.blockID;
	}

	public Behavior setRange(int range) {
		this.fireRange = range;
		return this;
	}

	private boolean canNeighborBurn(World world, int x, int y, int z, int id) {
		Block block = Block.blocksList[id];
		int meta = world.getBlockMetadata(x, y, z);
		return block == null || block.isFlammable(world, x, y, z, meta, WEST) || block.isFlammable(world, x, y, z, meta, EAST) || block.isFlammable(world, x, y, z, meta, UP)
				|| block.isFlammable(world, x, y, z, meta, DOWN) || block.isFlammable(world, x, y, z, meta, SOUTH) || block.isFlammable(world, x, y, z, meta, NORTH);
	}
}
