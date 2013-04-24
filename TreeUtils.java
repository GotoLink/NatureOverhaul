package mods.natureoverhaul;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class TreeUtils {
	private static final int MAX_TREE_HEIGHT = 16;
	// Flag controls
	private static final int iBits = 5,jBits = 7,kBits = 5;
	/** 
	* The radius at which leaves are destroyed by the nature overhaul
	* algorithm providing that the option for leaf decay is enabled
	* and there is no wood within this radius
	*/
	public static int leafDeathRadius = 2;
	
	/**
	* Check if current block is a part of a tree trunk
	*
	* @param	ignoreSelf		Ignores the block status at i,j,k when true
	* @return	True if is a part of a tree
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
		int curI = i;
		int curJ = j - 1;
		int curK = k;
		// Look down first
		while((checked <= MAX_TREE_HEIGHT) && (!groundFound) && (!isNotTree)) {
			int blockBelowID = world.getBlockId(curI, curJ, curK);
			if(Utils.getType(blockBelowID) == type) {
				curJ = curJ - 1;
			} else if((blockBelowID == Block.dirt.blockID) || Utils.getType(blockBelowID) == NOType.GRASS) {
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
		if((!isNotTree) && (groundFound)) {
			while((checked <= MAX_TREE_HEIGHT) && (!topFound) && (!isNotTree)) {
				int blockAboveID = world.getBlockId(curI, curJ, curK);
				// Continue scanning for leaves
				// After || is ignoring self block
				if((Utils.getType(blockAboveID) == type) || ((curJ == j) && (ignoreSelf))) {
					if(allTypeAround(world, curI, curJ, curK, NOType.LEAVES)) {
						leafLayersFound++;
					}			
					curJ = curJ + 1;
				// Top of tree found
				} else if(blockAboveID == Block.leaves.blockID) {//TODO:What is the top for a mushroom tree ?
					topFound = true;
				} else {
					isNotTree = true;
				}
			}
			
			// Ground found is also true at this point by definition
			return (!isNotTree && topFound && (leafLayersFound > 0));
		} else {
			return false;
		}
	}

	/**
	* Check if a block is surrounded by given type
	*
	* @param type The NOType searched for
	* @return	True if surrounded by given type, on a horizontal plane
	*/
	public static boolean allTypeAround(World world, int i, int j, int k, NOType type) {
		return (Utils.getType(world.getBlockId(i + 1, j, k)) == type &&
				Utils.getType(world.getBlockId(i - 1, j, k)) == type &&
				Utils.getType(world.getBlockId(i, j, k + 1)) == type &&
				Utils.getType(world.getBlockId(i, j, k - 1)) == type);
	}
	
	/**
	* Kills a tree from the given block upwards
	*
	* @param	killLeaves	True if leaves should be killed
	* @return	Number of logs removed
	*/
	public static int killTree(World world, int i, int j, int k, int id, boolean killLeaves) {
		int treeHeight = getTreeHeight(world, i, j, k, id);
		
		// If ignore self (ie; self could be air, then skip over it)
		if(world.getBlockId(i,j,k) == 0) {
			j++;
		}
	
		//System.out.println("Killing tree from ("+i+","+j+","+k+") with height " + treeHeight);
		// Kill first log to avoid down scanning
		world.setBlockToAir(i, j, k);
		
		HashSet<Integer> flags = new HashSet<Integer>();
		
		int[] base  = {i, j, k};
		int[] block = {i, j + 1, k};
		flags.add(makeFlag(block, base));
		int out = 1 + scanAndFlag(world, base, block, flags, treeHeight);
		
		if(killLeaves) {
			killLeaves(world, i, j, k, base, flags);
		}	
		return out;
	}

	/**
	* Gets height of the tree based on the block id
	 * @param id 
	*/
	public static int getTreeHeight(World world, int i, int j, int k, int id) {
		int height = 1;
		int curJ = j - 1;
		// Down first
		while(world.getBlockId(i, curJ, k) == id) {
			curJ--;
			height++;
		}
		
		curJ = j + 1;
		while(world.getBlockId(i, curJ, k) == id) {
			curJ++;
			height++;
		}
		
		return height;
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
		// to search in the x/z of the inRange algoirthm
		int flag = (int) (i << (jBits + kBits)) ^ (j << kBits) ^ k;

		//System.out.println("Flag pieces: " + i + ", " + j + ", " + k  +". Result: " + flag);
		
		return flag;
	}
	/**
	* Scans, deletes and flags blocks for lumberjacking
	*
	* @param	block		 Current block to scan and flag
	* @param	base			 The base block
	* @param	flags		 Flag store
	* @return	Number of blocks removed
	*/
	private static int scanAndFlag(World world, int[] base, int[] block, 
						HashSet<Integer> flags, int treeHeight) {
		int i = block[0];
		int j = block[1];
		int k = block[2];
		int removed = 0;
		
		//System.out.println("Scan and flag (" + i + ","+j+","+k+")");
		
		for(int[] nBlock : neighbours(block)) {
			int id = world.getBlockId(nBlock[0], nBlock[1], nBlock[2]);
			if((inRange(nBlock, base,treeHeight)) && (!flags.contains(makeFlag(nBlock, base)))
				&& ((id == Block.wood.blockID) || (id == Block.leaves.blockID))) {
				flags.add(makeFlag(nBlock, base));
				removed = removed + scanAndFlag(world, base, nBlock, flags, treeHeight);
			}
		}
		
		// Remove the current block if it's a non-tree log
		if(Utils.getType(world.getBlockId(i,j,k)) == NOType.LOG) {
			if((!isTree(world, i, j, k, Utils.getType(world.getBlockId(i,j,k)), false)) || ((i == base[0]) && (k == base[2]))) {
				world.setBlockToAir(block[0], block[1], block[2]);
				removed++;
			}
		}	
		return removed;
	}

	/**
	* Get all blocks connected by a face to given block
	*/
	public static int[][] neighbours(int[] block) {
		int i = block[0];
		int j = block[1];
		int k = block[2];
		
		int[][] n = {
			{i + 1, j, k},
			{i - 1, j, k},
			{i, j + 1, k},
			{i, j - 1, k},
			{i, j, k + 1},
			{i, j, k - 1}
		};	
		return n;
	}

	/**
	* Check if a block is in range of the base
	*/
	private static boolean inRange(int[] block, int[] base, int treeHeight) {
		// Check the x/z are within a 6 square
		if((Math.abs(block[0] - base[0]) <= 6) 
		&& (Math.abs(block[2] - base[2]) <= 6)){
			// Within the tree height plus a little for leaves
			if((block[1] >= base[1]) &&
				(block[1] - base[1] <= treeHeight + 5)) {
				return true;
			}
		}
		
		return false;
	}
	/**
	* Scans through the flags for leaves and removes them
	*
	* @param	flags	The encoded flags
	* @param	base		The base block (for reference)
	*/
	public static void killLeaves(World world, int i, int j, int k, int[] base, HashSet<Integer> flags) {
		for(Integer flag : flags) {
			int iMod = decodeFlag(flag, iBits, jBits + kBits);
			int jMod = decodeFlag(flag, jBits, kBits);
			int kMod = decodeFlag(flag, kBits, 0);
			int id=Block.wood.blockID;//TODO:change this
			int lI = iMod + base[0];
			int lJ = jMod + base[1];
			int lK = kMod + base[2];
			
			//System.out.println("iMod: " + iMod + ". jMod: " + jMod + ". kMod: " + kMod);
			
			//System.out.println("Kill Leaf: (" + lI + ", " + lJ + ", " + lK + "");
			if(!Utils.hasNearbyBlock(world, lI, lJ, lK, id, leafDeathRadius, false)) {
				world.setBlockToAir(lI, lJ, lK);
			}
		}
	}
	/**
	* Decode the modifier of a flag
	* 
	* @param	flag
	* @param	len 		length of entry
	* @param	shift	How far left the value is shifted
	* @return	Int value
	*/
	private static int decodeFlag(int flag, int len, int shift) {
		return ((flag >> shift) & ((int) Math.pow(2, len) - 1)) - (int) Math.pow(2, len - 1);
	}

	public static void growTree(World world, int i, int j, int k, int id, NOType type) {
		int base = Utils.getLowestTypeJ(world, i, j, k, type);
		//int meta = world.getBlockMetadata(i, j, k);//TODO:Use metadata
		boolean onBranch=false;
		int[] node=new int[]{i,base,k};
		int[] branch=new int[3];
		while(world.getBlockId(node[0], node[1], node[2]) == id && !onBranch)
		{
			branch=findValidNeighbor(world, node[0], node[1], node[2], id);
			if (branch!=null)
			{		
				onBranch=true;
			}else
			node[1]++;
		}
		if(!onBranch)//We went to the top
		{
			world.setBlock(node[0], node[1], node[2], id);
			world.setBlock(node[0], node[1]+1, node[2], Block.leaves.blockID);
		}
		else//We are on a branch
		{	
			byte branchLength=0;
			int[] newBranch=findValidNeighbor(world, branch[0], branch[1], branch[2], id);
			while(newBranch!=null && newBranch!=node && branchLength<8)//We don't want to go in cycle or make too long branch
			{
				branchLength++;
				node=branch;
				branch=newBranch;
				newBranch=findValidNeighbor(world, branch[0], branch[1], branch[2], id);
			}
			int[] leaf=findValidNeighbor(world, branch[0], branch[1], branch[2], 0);
			while(leaf!=null)
			{
				world.setBlock(leaf[0], leaf[1], leaf[2], Block.leaves.blockID);
				leaf=findValidNeighbor(world, branch[0], branch[1], branch[2], 0);
			}
		}
		System.out.println("done growing");
	}

	private static int[] findValidNeighbor(World world, int i, int j, int k, int id) {
		int[][] n=neighbours(new int[]{i,j,k});
		for (int x=0;x<5;x++)
		{
			if (x!=2 && x!=3)
			{
				if (world.getBlockId(n[x][0],n[x][1],n[x][2])==id)
				{
					return n[x];							
				}
			}
		}
		return null;		
	}
}
