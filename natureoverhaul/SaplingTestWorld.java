package natureoverhaul;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * A controlled world instance to test for sapling behavior
 */
public final class SaplingTestWorld extends World{
    /**
     * The space within which blocks can change : a chunk centered on the sapling, roughly
     */
    private Chunk environment;
    /**
     * The covered world
     */
    private final WorldServer wrapped;

    public SaplingTestWorld(WorldServer world){
        super(world.getSaveHandler(), world.getWorldInfo().getWorldName(), null, world.provider, world.theProfiler);
        this.wrapped = world;
        this.chunkProvider = this.wrapped.getChunkProvider();
        //No need for those data:
        this.mapStorage = null;
        this.villageCollectionObj = null;
        this.worldScoreboard = null;
    }

    /**
     * The main tree detection logic.
     * Setups a new center chunk in which a centered sapling is applied bonemeal.
     * If bonemeal is consumed, scan the area for logs and leaves.
     *
     * @param sapling the block type to bonemeal
     * @param saplingMeta the subtype
     * @return the tree data, if two different block type have been set after bonemeal is applied
     */
    public TreeData getTree(Block sapling, int saplingMeta){
        int h = getHeight()/4;
        this.environment = new Chunk(this, 0, 0);
        this.environment.func_150807_a(7 & 15, h, 7 & 15, sapling, saplingMeta);
        int tries = 0;
        do {
            tries++;
            if(tries == 50){//A lot of fail, try the "big tree" setup
                this.environment = new Chunk(this, 0, 0);
                int min = 7 & 15, max = 8 & 15;
                this.environment.func_150807_a(min, h, min, sapling, saplingMeta);
                this.environment.func_150807_a(min, h, max, sapling, saplingMeta);
                this.environment.func_150807_a(max, h, min, sapling, saplingMeta);
                this.environment.func_150807_a(max, h, max, sapling, saplingMeta);
            }
            boolean success;
            try{
                success = ItemDye.applyBonemeal(new ItemStack(Items.dye, 1, 15), this, 7, h, 7, FakePlayerFactory.getMinecraft(wrapped));
            }catch (Exception ignored){
                success = false;
            }
            if(success) {//Bonemeal has been consumed
                int y = h - 1;
                int maxY = getTopSolidBlock(7, 7);//The top of a tree, hopefully
                if(maxY == -1){
                    maxY = getHeight();
                }
                Block tempTrunk;
                do {
                    y++;
                    tempTrunk = getBlock(7, y, 7);
                } while (y < maxY && (tempTrunk.getMaterial() == Material.air || tempTrunk == sapling));
                if (y < maxY) {//A new block has been set, above the sapling, assume a trunk
                    int trunkMeta = getBlockMetadata(7, y, 7);
                    for(; y < maxY; y++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                Block tempLeaf = getBlock(x, y, z);
                                if (tempLeaf.getMaterial() != Material.air && tempLeaf != sapling && tempLeaf != tempTrunk) {//A third type of block, surrounding the rest, assume leaves
                                    int leafMeta = getBlockMetadata(x, y, z);
                                    return new TreeData(sapling, tempTrunk, tempLeaf, saplingMeta, trunkMeta, leafMeta);
                                }
                            }
                        }
                    }
                }
            }
        }while(tries<100);//Should cover all randomness
        return null;
    }

    private int getTopSolidBlock(int x, int z) {
        Chunk chunk = this.environment;
        int k = chunk.getTopFilledSegment() + 15;
        for (; k > 0; --k)
        {
            Block block = chunk.getBlock(x & 15, k, z & 15);

            if (block.getMaterial().blocksMovement() && block.getMaterial().isSolid())
            {
                return k + 1;
            }
        }
        return -1;
    }

    /**
     * Clear the world data, allows GC
     */
    public void clearProvider(){
        this.wrapped.provider.registerWorld(this.wrapped);
        this.environment = null;
    }

    //don't try to setup stuff on the covered world
    @Override
    protected void initialize(WorldSettings settings){
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        if(y < getHeight()/4){
            return Blocks.grass;
        }
        if(y < getHeight()/2 && chunkExists(x>>4, z>>4)) {
            return this.environment.getBlock(x & 15, y, z & 15);
        }
        return Blocks.air;//Most of the world is made of air
    }

    @Override
    protected boolean chunkExists(int chunkX, int chunkZ){
        return chunkX == 0 && chunkZ == 0;
    }

    @Override
    public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ){
        if(chunkExists(chunkX, chunkZ)){
            return environment;
        }
        return super.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override
    public boolean setBlock(int x, int y, int z, Block block, int meta, int flag){
        if(y >= 0 && y < getHeight() && chunkExists(x>>4, z>>4)){
            return environment.func_150807_a(x & 15, y, z & 15, block, meta);
        }
        return false;
    }

    @Override
    public void markAndNotifyBlock(int x, int y, int z, Chunk chunk, Block oldBlock, Block newBlock, int flag){
    }

    @Override
    public boolean setBlockMetadataWithNotify(int x, int y, int z, int meta, int flag){
        if(y >= 0 && y < getHeight() && chunkExists(x>>4, z>>4)){
            return environment.setBlockMetadata(x & 15, y, z & 15, meta);
        }
        return false;
    }

    @Override//Prevent harvesting drops
    public boolean func_147480_a(int x, int y, int z, boolean doDrop){
        return super.func_147480_a(x, y, z, false);
    }

    @Override
    public void markBlocksDirtyVertical(int x, int z, int minY, int maxY){
    }

    @Override
    public void notifyBlockOfNeighborChange(int x, int y, int z, final Block block){
    }

    @Override//Some raytracing
    public MovingObjectPosition func_147447_a(Vec3 vec3, Vec3 vec, boolean bool, boolean bool1, boolean bool2){
        return null;
    }

    @Override
    public void playSoundAtEntity(Entity entity, String sound, float volume, float pitch){
    }

    @Override
    public void playSoundToNearExcept(EntityPlayer player, String sound, float volume, float pitch){
    }

    @Override
    public boolean addWeatherEffect(Entity entity){
        return false;
    }

    @Override
    public boolean spawnEntityInWorld(Entity entity){
        return false;
    }

    @Override
    public void removeEntity(Entity entity){
    }

    @Override
    public void removePlayerEntityDangerously(Entity entity){
    }

    @Override
    public void addWorldAccess(IWorldAccess access){
    }

    @Override
    public List getCollidingBoundingBoxes(Entity entity, AxisAlignedBB box){
        return new ArrayList();
    }

    @Override
    public List func_147461_a(AxisAlignedBB box){
        return new ArrayList();
    }

    @Override
    public void updateEntities(){}

    @Override
    public void updateEntityWithOptionalForce(Entity entity, boolean forceUpdate){}

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return null;
    }

    @Override
    public void setTileEntity(int x, int y, int e, TileEntity entity){}

    @Override
    public void setAllowedSpawnTypes(boolean bool1, boolean bool2){}

    @Override
    public void tick(){}

    @Override
    protected void updateWeather(){}

    @Override
    public void updateWeatherBody(){}

    @Override
    protected void setActivePlayerChunksAndCheckLight(){}

    @Override
    public List getEntitiesWithinAABBExcludingEntity(Entity entity, AxisAlignedBB box, IEntitySelector selector){
        return new ArrayList();
    }

    @Override
    public List selectEntitiesWithinAABB(Class type, AxisAlignedBB box, IEntitySelector selector){
        return new ArrayList();
    }

    @Override
    protected int func_152379_p() {
        return 0;
    }

    @Override
    public Entity getEntityByID(int id) {
        return null;
    }

    @Override
    public void addLoadedEntities(List list){}

    @Override
    public void unloadEntities(List list){}

    @Override
    public void checkSessionLock() throws MinecraftException{}

    @Override
    public void setWorldTime(long p_72877_1_){}

    @Override
    public void setSpawnLocation(int x, int y, int z){}

    @Override
    public void setItemData(String key, WorldSavedData value){}

    @Override
    public WorldSavedData loadItemData(Class type, String key){
        return null;
    }

    @Override
    public int getUniqueDataId(String key){
        return 0;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return wrapped!=null ? wrapped.getChunkProvider(): null;
    }

    @Override
    public void addTileEntity(TileEntity entity){}
}
