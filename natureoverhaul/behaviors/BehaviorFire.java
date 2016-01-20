package natureoverhaul.behaviors;

import natureoverhaul.NatureOverhaul;
import natureoverhaul.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BehaviorFire extends BehaviorDeathDisappear {
	protected int limit = 15;
    public BehaviorFire(){
        super(null, 30);
    }

	@Override
	public void death(World world, BlockPos pos, IBlockState id) {
		super.death(world, pos, id);
		if (isBurnableGround(world.getBlockState(pos.down()).getBlock())) {
			world.setBlockState(pos.down(), getBurnedGround());
		}
	}

	public IBlockState getBurnedGround() {
		return Blocks.dirt.getDefaultState();
	}

	public int getRange() {
		return NatureOverhaul.INSTANCE.fireRange;
	}

	@Override
	public void grow(World world, BlockPos pos, IBlockState block) {
		if (block.getBlock() instanceof BlockFire) {
			int l = (Integer) block.getValue(BlockFire.AGE);
			if (l > 0) {
				world.setBlockState(pos, block.withProperty(BlockFire.AGE, l-1), 4);
			}
			if (isBurnableGround(world.getBlockState(pos.down()).getBlock())) {
				world.setBlockState(pos.down(), getBurnedGround());
			}
		}
		BlockPos neighbour;
		Block nId;
        int tries = 0;
		while (tries < limit) {
			neighbour = Utils.findRandomNeighbour(pos, getRange());
			nId = world.getBlockState(neighbour).getBlock();
			if (canNeighborBurn(world, neighbour, nId)) {
				world.setBlockState(neighbour, block);
			}
			tries++;
		}
	}

	public boolean isBurnableGround(Block id) {
		return id == Blocks.grass;
	}

	private boolean canNeighborBurn(World world, BlockPos pos, Block block) {
		return block.isAir(world, pos) || block.isFlammable(world, pos, EnumFacing.WEST) || block.isFlammable(world, pos, EnumFacing.EAST) || block.isFlammable(world, pos, EnumFacing.UP)
				|| block.isFlammable(world, pos, EnumFacing.DOWN) || block.isFlammable(world, pos, EnumFacing.SOUTH) || block.isFlammable(world, pos, EnumFacing.NORTH);
	}
}
