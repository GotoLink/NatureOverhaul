package natureoverhaul;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler{
	private static World world;
	private static NatureOverhaul no=NatureOverhaul.instance;
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		world=(World) tickData[0];	
		Iterator it=world.activeChunkSet.iterator();
		ChunkCoordIntPair chunkIntPair=null;
		Chunk chunk=null;
		List<NextTickListEntry> list = null;
		NextTickListEntry nextTickEntry=null;
		while (it.hasNext())
		{
			chunkIntPair = (ChunkCoordIntPair) it.next();
			chunk=world.getChunkFromChunkCoords(chunkIntPair.chunkXPos,chunkIntPair.chunkZPos);
			if (chunk!=null)
			list=world.getPendingBlockUpdates(chunk, false);
			else break;
			if (list!=null)
			{
				Iterator itr=list.iterator();
				while(itr.hasNext())
				{
					nextTickEntry=(NextTickListEntry) itr.next();
					no.onUpdateTick(world,nextTickEntry.xCoord,nextTickEntry.yCoord,nextTickEntry.zCoord,nextTickEntry.blockID);
				}
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "Nature Overhaul Tick";
	}

}
