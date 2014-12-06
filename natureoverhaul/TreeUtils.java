package natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
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
	public static boolean allTypeAround(World world, int i, int j, int k, NOType type) {
		return (Utils.getType(world.getBlock(i + 1, j, k)) == type && Utils.getType(world.getBlock(i - 1, j, k)) == type && Utils.getType(world.getBlock(i, j, k + 1)) == type && Utils
				.getType(world.getBlock(i, j, k - 1)) == type);
	}

	/**
	 * Find coordinates with block of given id around the given coordinates
	 * 
	 * @param id
	 *            block id
	 * @param avoidVerticals
	 *            whether up and down blocks should be checked
	 */
	public static int[] findValidNeighbor(World world, int i, int j, int k, Block id, boolean avoidVerticals) {
		int[][] n = neighbours(new int[] { i, j, k });
		for (int x = 0; x < 5; x++) {
			if (avoidVerticals && x == 2)
				x = x + 2;
			if (world.getBlock(n[x][0], n[x][1], n[x][2]) == id)
				return n[x];
		}
		return null;
	}

	/**
	 * Gets height of a block vertical column based on the block id
	 * 
	 * @param id
	 */
	public static int getTreeHeight(World world, int i, int j, int k, Block id) {
		int height = 1;
		int curJ = j - 1;
		// Down first
		while (world.getBlock(i, curJ, k) == id) {
			curJ--;
			height++;
		}
		curJ = j + 1;
		while (world.getBlock(i, curJ, k) == id) {
			curJ++;
			height++;
		}
		return height;
	}

	public static void growTree(World world, int i, int j, int k, Block id, NOType type) {
		int lowJ = Utils.getLowestTypeJ(world, i, j, k, type);
        int meta = world.getBlockMetadata(i, lowJ, k);
        TreeData data = TreeData.getTree(id, meta, TreeData.Component.TRUNK);
        if(data==null)
            return;
		Block leaf = data.getBlock(TreeData.Component.LEAF);
        if(leaf==null||leaf==Blocks.air)
            return;
		int leafMeta = data.getMeta(TreeData.Component.LEAF);
		if (world.getBlock(i, lowJ - 1, k).getMaterial() == Material.ground || Utils.getType(world.getBlock(i, lowJ - 1, k)) == NOType.GRASS) {
			boolean branchFound = false;
			int[] node = new int[] { i, lowJ, k };
			List<int[]> branchs = new ArrayList<int[]>();
			int[] current;
			while ((node[1] - lowJ) <= MAX_TREE_HEIGHT && world.getBlock(node[0], node[1], node[2]) == id) {//Try to find a "branch" by looking for neighbor log block
				current = findValidNeighbor(world, node[0], node[1], node[2], id, true);
				if (current != null) {
					branchs.add(current);
					branchFound = true;
					current = null;
				}
				node[1]++;//Only accounts for straight up tree trunks
			}
			if (!branchFound)//We went to the top
			{
				world.setBlock(node[0], node[1], node[2], id, meta, 3);
                world.setBlock(node[0], node[1] + 1, node[2], leaf, leafMeta, 3);
                putBlocksAround(world, node[0], node[1], node[2], leaf, leafMeta);
			} else//We found at least a branch
			{
				current = branchs.get(world.rand.nextInt(branchs.size()));
				doBranching(world, leaf, id, meta, leafMeta, current, node);
			}
		} else {//We are on a branch, which might only be a weird floating log block
			doBranching(world, leaf, id, meta, leafMeta, new int[] { i, lowJ, k }, null);
		}
	}

	/**
	 * Check if current block is a part of a tree trunk
	 * 
	 * @param ignoreSelf
	 *            Ignores the block status at i,j,k when true
	 * @return True if is a part of a tree trunk
	 */
	public static boolean isTree(World world, int i, int j, int k, NOType type, boolean ignoreSelf) {
		// How many logs we have checked
		int checked = 0;
		boolean groundFound = false;
		// Top of tree found
		boolean topFound = false;
		boolean isNotTree = false;
		// Surrounding Leaves found
		int leafLayersFound = 0;
		int curJ = j - 1;
		// Look down first
		while (checked <= MAX_TREE_HEIGHT && !groundFound && !isNotTree) {
			Block blockBelowID = world.getBlock(i, curJ, k);
			if (Utils.getType(blockBelowID) == type) {
				curJ = curJ - 1;
			} else if (blockBelowID.getMaterial() == Material.ground || Utils.getType(blockBelowID) == NOType.GRASS) {
				groundFound = true;// We have found a ground block below
				// Put the J back onto a known log
				curJ = curJ + 1;
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
				Block blockAboveID = world.getBlock(i, curJ, k);
				// Continue scanning for leaves
				// After || is ignoring self block
				if (Utils.getType(blockAboveID) == type || (curJ == j && ignoreSelf)) {
					if ((type == NOType.LOG && allTypeAround(world, i, curJ, k, NOType.LEAVES)) || (type == NOType.MUSHROOMCAP && allTypeAround(world, i, curJ, k, NOType.MUSHROOMCAP))) {
						leafLayersFound++;
					}
					curJ = curJ + 1;
					// Top of tree found
				} else if ((type == NOType.LOG && Utils.getType(blockAboveID) == NOType.LEAVES) || (type == NOType.MUSHROOMCAP && allTypeAround(world, i, curJ, k, NOType.MUSHROOMCAP))) {
					topFound = true;
				} else {
					isNotTree = true;
				}
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
	public static void killLeaves(World world, int i, int j, int k, Block id, int[] base, HashSet<Integer> flags) {
		for (Integer flag : flags) {
			int iMod = decodeFlag(flag, iBits, jBits + kBits);
			int jMod = decodeFlag(flag, jBits, kBits);
			int kMod = decodeFlag(flag, kBits, 0);
			int lI = iMod + base[0];
			int lJ = jMod + base[1];
			int lK = kMod + base[2];
			//System.out.println("iMod: " + iMod + ". jMod: " + jMod + ". kMod: " + kMod);
			//System.out.println("Kill Leaf: (" + lI + ", " + lJ + ", " + lK + "");
			if (!Utils.hasNearbyBlock(world, lI, lJ, lK, id, leafDeathRadius, false)) {
				world.setBlockToAir(lI, lJ, lK);
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
	public static int killTree(World world, int i, int j, int k, Block id, boolean killLeaves) {
		int treeHeight = getTreeHeight(world, i, j, k, id);
		// If ignore self (ie; self could be air, then skip over it)
		if (world.getBlock(i, j, k) == Blocks.air) {
			j++;
		}
		//System.out.println("Killing tree from ("+i+","+j+","+k+") with height " + treeHeight);
		// Kill first log to avoid down scanning
		world.setBlockToAir(i, j, k);
		HashSet<Integer> flags = new HashSet<Integer>();
		int[] base = { i, j, k };
		int[] block = { i, j + 1, k };
		flags.add(makeFlag(block, base));
		int out = 1 + scanAndFlag(world, base, block, flags, treeHeight);
		if (killLeaves) {
			killLeaves(world, i, j, k, id, base, flags);
		}
		return out;
	}

	/**
	 * Get all blocks connected by a face to given block
	 */
	public static int[][] neighbours(int[] block) {
		int i = block[0];
		int j = block[1];
		int k = block[2];
		return new int[][]{ { i + 1, j, k }, { i - 1, j, k }, { i, j + 1, k }, { i, j - 1, k }, { i, j, k + 1 }, { i, j, k - 1 } };
	}

	/**
	 * Put blocks with given id and metadata around the given coordinates if
	 * they are empty
	 * 
	 * @param id
	 *            block id
	 * @param meta
	 *            block id
	 */
	public static void putBlocksAround(World world, int i, int j, int k, Block id, int meta) {
		int[] leaf = findValidNeighbor(world, i, j, k, Blocks.air, false);
		while (leaf != null) {
			world.setBlock(leaf[0], leaf[1], leaf[2], id, meta, 3);
			leaf = findValidNeighbor(world, i, j, k, Blocks.air, false);
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
	 * @param id
	 *            The log block id
	 * @param meta
	 *            The log block metadata
	 * @param leafMeta
	 *            The leaf block metadata
	 * @param current
	 *            Coordinates of a block we know is on a branch
	 * @param node
	 *            Coordinates of a block we know is on the trunk, or null
	 */
	private static void doBranching(World world, Block leaf, Block id, int meta, int leafMeta, int[] current, int[] node) {
		byte branchLength = 0;
		int[] newBranch = findValidNeighbor(world, current[0], current[1], current[2], id, false);
		while (newBranch != null && newBranch != node && branchLength < 8)//We don't want to go in cycle or make too long branch
		{
			branchLength++;
			node = current;
			current = newBranch;
			newBranch = findValidNeighbor(world, current[0], current[1], current[2], id, false);
		}
		newBranch = findValidNeighbor(world, current[0], current[1], current[2], Blocks.air, false);
		if (newBranch != null) {
			world.setBlock(newBranch[0], newBranch[1], newBranch[2], id, meta, 3);
            putBlocksAround(world, newBranch[0], newBranch[1], newBranch[2], leaf, leafMeta);
		} else
			putBlocksAround(world, current[0], current[1], current[2], leaf, leafMeta);
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
	private static int scanAndFlag(World world, int[] base, int[] block, HashSet<Integer> flags, int treeHeight) {
		int i = block[0];
		int j = block[1];
		int k = block[2];
		int removed = 0;
		//System.out.println("Scan and flag (" + i + ","+j+","+k+")");
		for (int[] nBlock : neighbours(block)) {
			Block id = world.getBlock(nBlock[0], nBlock[1], nBlock[2]);
			if (inRange(nBlock, base, treeHeight) && !flags.contains(makeFlag(nBlock, base))
					&& ((Utils.getType(id) == NOType.LOG || Utils.getType(id) == NOType.MUSHROOMCAP) || Utils.getType(id) == NOType.LEAVES)) {
				flags.add(makeFlag(nBlock, base));
				removed = removed + scanAndFlag(world, base, nBlock, flags, treeHeight);
			}
		}
		// Remove the current block if it's a non-tree log
		NOType type = Utils.getType(world.getBlock(i, j, k));
		if (type == NOType.LOG || type == NOType.MUSHROOMCAP) {
			if (!isTree(world, i, j, k, type, false) || (i == base[0] && k == base[2])) {
				world.setBlockToAir(block[0], block[1], block[2]);
				removed++;
			}
		}
		return removed;
	}
}
