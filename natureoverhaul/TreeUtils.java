package natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Clinton Alexander
 */
public final class TreeUtils {
    private static final int MIN_LEAF_LAYER = 1;//Let's consider a leaf layer is enough
	private static final int MAX_TREE_HEIGHT = 16;
    private static final int MAX_RANGE = 6;
	// Flag controls
	private static final int iBits = 5, jBits = 7, kBits = 5;
	/**
	 * The radius at which leaves are destroyed by the nature overhaul algorithm
	 * providing that the option for leaf decay is enabled and there is no wood
	 * within this radius
	 */
	public static int leafDeathRadius = 2;

	/**
	 * Check if a block is surrounded by given type
	 * 
	 * @param type
	 *            The NOType searched for
	 * @return True if surrounded by given type, on a horizontal plane
	 */
	public static boolean allTypeAround(World world, BlockPos pos, NOType type) {
		return (Utils.getType(world.getBlockState(pos.east())) == type && Utils.getType(world.getBlockState(pos.west())) == type && Utils.getType(world.getBlockState(pos.south())) == type && Utils
				.getType(world.getBlockState(pos.north())) == type);
	}

	/**
	 * Find coordinates with block of given id around the given coordinates
	 * 
	 * @param id
	 *            block id
	 * @param avoidVerticals
	 *            whether up and down blocks should be checked
	 */
	public static BlockPos findValidNeighbor(World world, BlockPos pos, IBlockState id, boolean avoidVerticals) {
		BlockPos[] n = neighbours(pos);
		for (int x = 0; x < 5; x++) {
			if (avoidVerticals && x == 2)
				x = x + 2;
			if (Utils.equal(world.getBlockState(n[x]), id))
				return n[x];
		}
		return null;
	}

	/**
	 * Gets height of a block vertical column based on the block id
	 * 
	 * @param id
	 */
	public static int getTreeHeight(World world, BlockPos pos, IBlockState id) {
		int height = 1;
		BlockPos curJ = pos.down();
		// Down first
		while (Utils.equal(world.getBlockState(curJ), id)) {
			curJ = curJ.down();
			height++;
		}
		curJ = pos.up();
		while (Utils.equal(world.getBlockState(curJ), id)) {
			curJ = curJ.up();
			height++;
		}
		return height;
	}

	public static void growTree(World world, BlockPos pos, IBlockState id, NOType type) {
		BlockPos lowJ = Utils.getLowestType(world, pos, type);
        IBlockState meta = world.getBlockState(lowJ);
        TreeData data = TreeData.getTree(meta, TreeData.Component.TRUNK);
        if(data==null)
            return;
		Block leaf = data.getBlock(TreeData.Component.LEAF);
        if(leaf==null||leaf==Blocks.air)
            return;
		IBlockState leafMeta = data.getState(TreeData.Component.LEAF);
		if (world.getBlockState(lowJ.down()).getBlock().getMaterial() == Material.ground || Utils.getType(world.getBlockState(lowJ.down())) == NOType.GRASS) {
			boolean branchFound = false;
			BlockPos node = lowJ;
			List<BlockPos> branchs = new ArrayList<BlockPos>();
			BlockPos current;
			while ((node.getY() - lowJ.getY()) <= MAX_TREE_HEIGHT){
				IBlockState state = world.getBlockState(node);
				if(state.getBlock() == id.getBlock()) {//Try to find a "branch" by looking for neighbor log block
					current = findValidNeighbor(world, node, state, true);
					if (current != null) {
						branchs.add(current);
						branchFound = true;
						current = null;
					}
					node = node.up();//Only accounts for straight up tree trunks
				}else{
					break;
				}
			}
			if (!branchFound)//We went to the top
			{
				world.setBlockState(node, meta);
                world.setBlockState(node.up(), leafMeta);
                putBlocksAround(world, node, leafMeta);
			} else//We found at least a branch
			{
				current = branchs.get(world.rand.nextInt(branchs.size()));
				doBranching(world, leafMeta, meta, current, node);
			}
		} else {//We are on a branch, which might only be a weird floating log block
			doBranching(world, leafMeta, meta, lowJ, null);
		}
	}

	/**
	 * Check if current block is a part of a tree trunk
	 * 
	 * @param ignoreSelf
	 *            Ignores the block status at i,j,k when true
	 * @return True if is a part of a tree trunk
	 */
	public static boolean isTree(World world, BlockPos pos, NOType type, boolean ignoreSelf) {
		// How many logs we have checked
		int checked = 0;
		boolean groundFound = false;
		// Top of tree found
		boolean topFound = false;
		boolean isNotTree = false;
		// Surrounding Leaves found
		int leafLayersFound = 0;
		BlockPos curJ = pos.down();
		// Look down first
		while (checked <= MAX_TREE_HEIGHT && !groundFound && !isNotTree) {
			IBlockState blockBelowID = world.getBlockState(curJ);
			if (Utils.getType(blockBelowID) == type) {
				curJ = curJ.down();
			} else if (blockBelowID.getBlock().getMaterial() == Material.ground || Utils.getType(blockBelowID) == NOType.GRASS) {
				groundFound = true;// We have found a ground block below
				// Put the J back onto a known log
				curJ = curJ.up();
			} else {
				isNotTree = true;// It has been proven this is not a tree
			}
			checked++;
		}
		// Set checked back to 0 as we are scanning the whole tree up now
		checked = 0;
		// Scan back up for leaves
		if (!isNotTree && groundFound) {
			while (checked <= MAX_TREE_HEIGHT && !topFound && !isNotTree) {
				IBlockState blockAboveID = world.getBlockState(curJ);
				// Continue scanning for leaves
				// After || is ignoring self block
				if (Utils.getType(blockAboveID) == type || (curJ.getY() == pos.getY() && ignoreSelf)) {
					if ((type == NOType.LOG && allTypeAround(world, curJ, NOType.LEAVES)) || (type == NOType.MUSHROOMCAP && allTypeAround(world, curJ, NOType.MUSHROOMCAP))) {
						leafLayersFound++;
					}
					curJ = curJ.up();
					// Top of tree found
				} else if ((type == NOType.LOG && Utils.getType(blockAboveID) == NOType.LEAVES) || (type == NOType.MUSHROOMCAP && allTypeAround(world, curJ, NOType.MUSHROOMCAP))) {
					topFound = true;
				} else {
					isNotTree = true;
				}
				checked ++;
			}
			// Ground found is also true at this point by definition
			return (!isNotTree && (topFound || leafLayersFound > MIN_LEAF_LAYER-1));
		} else {
			return false;
		}
	}

	/**
	 * Scans through the flags for leaves and removes them
	 * 
	 * @param id
	 *            Log id which can sustain the leaves
	 * @param flags
	 *            The encoded flags
	 * @param base
	 *            The base block (for reference)
	 */
	public static void killLeaves(World world, IBlockState id, int[] base, HashSet<Integer> flags) {
		for (Integer flag : flags) {
			int iMod = decodeFlag(flag, iBits, jBits + kBits);
			int jMod = decodeFlag(flag, jBits, kBits);
			int kMod = decodeFlag(flag, kBits, 0);
			BlockPos pos = new BlockPos(iMod + base[0], jMod + base[1], kMod + base[2]);
			if (!Utils.hasNearbyBlock(world, pos, id, leafDeathRadius, false)) {
				world.setBlockToAir(pos);
			}
		}
	}

	/**
	 * Kills a tree from the given block upwards
	 * 
	 * @param killLeaves
	 *            True if leaves should be killed
	 * @return Number of logs removed
	 */
	public static int killTree(World world, BlockPos pos, IBlockState id, boolean killLeaves) {
		int treeHeight = getTreeHeight(world, pos, id);
		// If ignore self (ie; self could be air, then skip over it)
		if (world.isAirBlock(pos)) {
			pos = pos.up();
		}
		//System.out.println("Killing tree from ("+i+","+j+","+k+") with height " + treeHeight);
		// Kill first log to avoid down scanning
		world.setBlockToAir(pos);
		HashSet<Integer> flags = new HashSet<Integer>();
		int[] base = new int[]{pos.getX(), pos.getY(), pos.getZ()};
		int[] block = new int[]{pos.getX(), pos.getY()+1, pos.getZ()};
		flags.add(makeFlag(block, base));
		int out = 1 + scanAndFlag(world, base, pos.up(), flags, treeHeight);
		if (killLeaves) {
			killLeaves(world, id, base, flags);
		}
		return out;
	}

	/**
	 * Get all blocks connected by a face to given block
	 */
	public static BlockPos[] neighbours(BlockPos pos) {
		return new BlockPos[]{ pos.east(), pos.west(), pos.up(), pos.down(), pos.south(), pos.north() };
	}

	/**
	 * Put blocks with given id and metadata around the given coordinates if
	 * they are empty
	 * 
	 * @param id block state
	 */
	public static void putBlocksAround(World world, BlockPos pos, IBlockState id) {
		BlockPos leaf = findValidNeighbor(world, pos, Blocks.air.getDefaultState(), false);
		while (leaf != null) {
			world.setBlockState(leaf, id);
			leaf = findValidNeighbor(world, pos, Blocks.air.getDefaultState(), false);
		}
	}

	/**
	 * Decode the modifier of a flag
	 * 
	 * @param flag
	 * @param len
	 *            length of entry
	 * @param shift
	 *            How far left the value is shifted
	 * @return Int value
	 */
	private static int decodeFlag(int flag, int len, int shift) {
		return ((flag >> shift) & ((int) Math.pow(2, len) - 1)) - (int) Math.pow(2, len - 1);
	}

	/**
	 * Try to enlarge the branch at given coordinates by going from neighbor to neighbor
	 * 
	 * @param world
	 * @param leaf
	 *            The leaf block id linked to the log id
	 * @param log
	 *            The log block id
	 * @param current
	 *            Coordinates of a block we know is on a branch
	 * @param node
	 *            Coordinates of a block we know is on the trunk, or null
	 */
	private static void doBranching(World world, IBlockState leaf, IBlockState log, BlockPos current, BlockPos node) {
		byte branchLength = 0;
		BlockPos newBranch = findValidNeighbor(world, current, log, false);
		while (newBranch != null && newBranch != node && branchLength < 8)//We don't want to go in cycle or make too long branch
		{
			branchLength++;
			node = current;
			current = newBranch;
			newBranch = findValidNeighbor(world, current, log, false);
		}
		newBranch = findValidNeighbor(world, current, Blocks.air.getDefaultState(), false);
		if (newBranch != null) {
			world.setBlockState(newBranch, log);
            putBlocksAround(world, newBranch, leaf);
		} else
			putBlocksAround(world, current, leaf);
	}

	/**
	 * Check if a block is in range of the base
	 */
	private static boolean inRange(int[] block, int[] base, int treeHeight) {
		// Check the x/z are within a 6 square
		if ((Math.abs(block[0] - base[0]) <= MAX_RANGE) && (Math.abs(block[2] - base[2]) <= MAX_RANGE)) {
			// Within the tree height plus a little for leaves
			if (block[1] >= base[1] && (block[1] - base[1] <= treeHeight + 5)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a flag from a block, based on distance from the base
	 */
	private static int makeFlag(int[] block, int base[]) {
		int i = ((block[0] - base[0]) % (int) Math.pow(2, iBits - 1)) + (int) Math.pow(2, iBits - 1);
		int j = ((block[1] - base[1]) % (int) Math.pow(2, jBits - 1)) + (int) Math.pow(2, jBits - 1);
		int k = ((block[2] - base[2]) % (int) Math.pow(2, kBits - 1)) + (int) Math.pow(2, kBits - 1);
		// Put i j+k bits in front so no clash with j/k
		// put j in the jth to kth bits
		// put k in the kth bits.
		// This way we can store i,j,k  as a single int
		// The size of i,j,kBits depends on the distance
		// to search in the x/z of the inRange algorithm
		//System.out.println("Flag pieces: " + i + ", " + j + ", " + k  +". Result: " + flag);
		return i << (jBits + kBits) ^ (j << kBits) ^ k;
	}

	/**
	 * Scans, deletes and flags blocks for lumberjacking
	 * 
	 * @param block
	 *            Current block to scan and flag
	 * @param base
	 *            The base block
	 * @param flags
	 *            Flag store
	 * @return Number of blocks removed
	 */
	private static int scanAndFlag(World world, int[] base, BlockPos block, HashSet<Integer> flags, int treeHeight) {
		int removed = 0;
		//System.out.println("Scan and flag " + block);
		for (BlockPos n : neighbours(block)) {
			int[] nBlock = new int[]{n.getX(), n.getY(), n.getZ()};
			if (inRange(nBlock, base, treeHeight) && !flags.contains(makeFlag(nBlock, base))){
				IBlockState id = world.getBlockState(n);
				if(((Utils.getType(id) == NOType.LOG || Utils.getType(id) == NOType.MUSHROOMCAP) || Utils.getType(id) == NOType.LEAVES)) {
					flags.add(makeFlag(nBlock, base));
					removed = removed + scanAndFlag(world, base, n, flags, treeHeight);
				}
			}
		}
		// Remove the current block if it's a non-tree log
		NOType type = Utils.getType(world.getBlockState(block));
		if (type == NOType.LOG || type == NOType.MUSHROOMCAP) {
			if (!isTree(world, block, type, false) || (block.getX() == base[0] && block.getZ() == base[2])) {
				world.setBlockToAir(block);
				removed++;
			}
		}
		return removed;
	}
}
