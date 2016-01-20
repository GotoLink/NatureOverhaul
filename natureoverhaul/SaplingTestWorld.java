package natureoverhaul;

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.*;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A controlled world instance to test for sapling behavior
 */
public final class SaplingTestWorld extends World{
    /**
     * The space within which blocks can change : a chunk centered on the sapling, roughly
     */
    private CoverChunk environment;
    /**
     * The covered world
     */
    private final WorldServer wrapped;

    public SaplingTestWorld(WorldServer world){
        super(world.getSaveHandler(), new WorldInfo(world.getWorldInfo()), new DummyProviderWrapper(world.provider), world.theProfiler, false);
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
     * @return the tree data, if two different block type have been set after bonemeal is applied
     */
    public TreeData getTree(IBlockState sapling){
        int h = getHeight()/4;
        this.environment = new CoverChunk();
        BlockPos center = new BlockPos(7, h, 7);
        this.environment.setBlockState(center, sapling);
        int tries = 0;
        do {
            tries++;
            if(tries == 25){//A lot of fail, try the "big tree" setup
                this.environment = new CoverChunk();
                this.environment.setBlockState(center, sapling);
                this.environment.setBlockState(center.south(), sapling);
                this.environment.setBlockState(center.east(), sapling);
                this.environment.setBlockState(center.south().east(), sapling);
            }
            boolean success;
            try{
                success = ItemDye.applyBonemeal(new ItemStack(Items.dye, 1, EnumDyeColor.WHITE.getDyeDamage()), this, center, FakePlayerFactory.getMinecraft(wrapped));
            }catch (Exception ignored){
                success = false;
            }
            if(success) {//Bonemeal has been consumed
                BlockPos trunk = center.down();
                int maxY = getTopSolidBlock(trunk);//The top of a tree, hopefully
                IBlockState tempTrunk;
                do {
                    trunk = trunk.up();
                    tempTrunk = this.environment.getBlockState(trunk);
                } while (trunk.getY() < maxY && (tempTrunk.getBlock().getMaterial() == Material.air || Utils.equal(tempTrunk, sapling)));
                int y = trunk.getY();
                if (y < maxY) {//A new block has been set, above the sapling, assume a trunk
                    for(; y < maxY; y++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {//Search around and above the trunk
                                IBlockState tempLeaf = this.environment.getBlockState(new BlockPos(x, y, z));
                                if (tempLeaf.getBlock().getMaterial() != Material.air && !Utils.equal(tempLeaf, sapling) && !Utils.equal(tempLeaf, tempTrunk)) {//A third type of block, assume leaves
                                    return new TreeData(sapling, tempTrunk, tempLeaf);
                                }
                            }
                        }
                    }
                }
            }
        }while(tries<50);//Should cover all randomness
        return null;
    }

    private int getTopSolidBlock(BlockPos pos) {
        int k = this.environment.getTopFilledSegment() + 15;
        for (; k > 0; --k)
        {
            Block block = this.environment.getBlock(new BlockPos(pos.getX(), k, pos.getZ()));

            if (block.getMaterial().blocksMovement() && block.getMaterial().isSolid())
            {
                return k + 1;
            }
        }
        return getHeight();
    }

    /**
     * Clear the world data, allows GC
     */
    public void clearProvider(){
        ((DummyProviderWrapper)this.provider).clear();
        this.environment.clear();
        this.environment = null;
        this.worldInfo = null;
    }

    //don't try to setup stuff on the covered world
    @Override
    public void initialize(WorldSettings settings){}

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if(pos.getY() < getHeight()/4){
            return Blocks.grass.getDefaultState();
        }
        if(pos.getY() < getHeight()/2 && isBlockLoaded(pos)) {
            return this.environment.getBlockState(pos);
        }
        return Blocks.air.getDefaultState();//Most of the world is made of air
    }

    protected boolean chunkExists(int chunkX, int chunkZ){
        return chunkX == 0 && chunkZ == 0 && environment != null;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty){
        return chunkExists(x, z);
    }

    @Override
    public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ){
        if(chunkExists(chunkX, chunkZ)){
            return environment;
        }
        return new EmptyChunk(wrapped, chunkX, chunkZ);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state, int flag) {
        return pos.getY() >= 0 && pos.getY() < getHeight() && isBlockLoaded(pos) && environment.setBlockState(pos, state) != null;
    }

    @Override
    public void markAndNotifyBlock(BlockPos pos, Chunk chunk, IBlockState oldBlock, IBlockState newBlock, int flag){}

    @Override//Prevent harvesting drops
    public boolean destroyBlock(BlockPos pos, boolean doDrop){
        return super.destroyBlock(pos, false);
    }

    @Override
    public void markBlocksDirtyVertical(int x, int z, int minY, int maxY){}

    @Override
    public void notifyBlockOfStateChange(BlockPos pos, final Block block){}

    @Override//Some raytracing
    public MovingObjectPosition rayTraceBlocks(Vec3 vec3, Vec3 vec, boolean bool, boolean bool1, boolean bool2){
        return null;
    }

    @Override
    public void playSoundAtEntity(Entity entity, String sound, float volume, float pitch){}

    @Override
    public void playSoundToNearExcept(EntityPlayer player, String sound, float volume, float pitch){}

    @Override
    public boolean addWeatherEffect(Entity entity){
        return false;
    }

    @Override
    public boolean spawnEntityInWorld(Entity entity){
        return false;
    }

    @Override
    public void removeEntity(Entity entity){}

    @Override
    public void removePlayerEntityDangerously(Entity entity){}

    @Override
    public void addWorldAccess(IWorldAccess access){}

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
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public void setTileEntity(BlockPos pos, TileEntity entity){}

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
    public List getEntitiesInAABBexcluding(Entity entity, AxisAlignedBB box, Predicate selector){
        return new ArrayList();
    }

    @Override
    public List getEntitiesWithinAABB(Class type, AxisAlignedBB box, Predicate selector){
        return new ArrayList();
    }

    @Override
    protected int getRenderDistanceChunks() {
        return 0;
    }

    @Override
    public Entity getEntityByID(int id) {
        return null;
    }

    @Override
    public int countEntities(Class entityType){
        return 0;
    }

    @Override
    public void loadEntities(Collection list){}

    @Override
    public void unloadEntities(Collection list){}

    @Override
    public void checkSessionLock() throws MinecraftException{}

    @Override
    public void setWorldTime(long p_72877_1_){}

    @Override
    public void setSpawnPoint(BlockPos pos){}

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
    public boolean addTileEntity(TileEntity entity){
        return false;
    }

    /**
     * Wrap the original WorldProvider, only allow getters to run
     */
    public static final class DummyProviderWrapper extends WorldProvider
    {
        private final WorldProvider wrapped;
        public DummyProviderWrapper(WorldProvider worldProvider){
            this.wrapped = worldProvider;
        }

        @Override protected void generateLightBrightnessTable(){}

        @Override
        @SideOnly(Side.CLIENT)
        public float[] calcSunriseSunsetColors(float x, float z)
        {
            return null;
        }

        @Override public String getDimensionName() { return wrapped.getDimensionName(); }

        @Override public String getInternalNameSuffix() {
            return wrapped.getInternalNameSuffix();
        }

        @Override public boolean doesWaterVaporize()
        {
            return wrapped.doesWaterVaporize();
        }

        @Override public boolean getHasNoSky()
        {
            return wrapped.getHasNoSky();
        }

        @Override public float[] getLightBrightnessTable() {
            return wrapped.getLightBrightnessTable();
        }

        @Override public int getDimensionId()
        {
            return wrapped.getDimensionId();
        }

        @Override public WorldBorder getWorldBorder()
        {
            return wrapped.getWorldBorder();
        }

        @Override public void setDimension(int dim){ }

        @Override public boolean canCoordinateBeSpawn(int x, int z){ return wrapped.canCoordinateBeSpawn(x, z); }

        @Override public BlockPos getSpawnCoordinate(){ return wrapped.getSpawnCoordinate(); }

        @Override public int getAverageGroundLevel() { return wrapped.getAverageGroundLevel(); }

        @Override public boolean isSurfaceWorld(){ return wrapped.isSurfaceWorld(); }

        @Override public boolean canRespawnHere(){ return wrapped.canRespawnHere(); }

        @Override public String getWelcomeMessage(){ return null; }

        @Override public String getDepartMessage(){ return null; }

        @Override public double getMovementFactor(){ return wrapped.getMovementFactor(); }

        @Override public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful) {}

        @Override public void calculateInitialWeather() {}

        @Override public void updateWeather() {}

        @Override public void setWorldTime(long time) {}

        @Override public void setSpawnPoint(BlockPos pos) {}

        @Override public void resetRainAndThunder(){}

        public void clear() {
            this.worldObj = null;
            //this.terrainType = null;
            this.worldChunkMgr = null;
        }
    }

    private class CoverChunk extends Chunk{

        public CoverChunk() {
            super(SaplingTestWorld.this, 0, 0);
        }

        @Override public boolean isLoaded()
        {
            return true;
        }

        @Override public void addEntity(Entity entityIn){}

        @Override public TileEntity getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType type){ return null; }

        @Override public void addTileEntity(TileEntity te){}

        @Override public void addTileEntity(BlockPos pos, TileEntity te){}

        @Override public void onChunkLoad(){}

        @Override public void onChunkUnload(){}

        @Override public void getEntitiesWithinAABBForEntity(Entity entity, AxisAlignedBB aabb, List list, Predicate predicate){}

        @Override public void getEntitiesOfTypeWithinAAAB(Class entityClass, AxisAlignedBB aabb, List list, Predicate predicate){}

        @Override public boolean needsSaving(boolean forced){ return false;}

        @Override public void populateChunk(IChunkProvider provider, IChunkProvider generator, int X, int Z){}

        @Override public boolean isPopulated(){ return true;}

        @Override public void setStorageArrays(ExtendedBlockStorage[] arrays){}

        public void clear(){
            Collection collection = getTileEntityMap().values();
            for(Object o: collection){
                if(o instanceof TileEntity){
                    ((TileEntity) o).setWorldObj(null);
                    ((TileEntity) o).invalidate();
                }
            }
            getTileEntityMap().clear();
        }
    }
}
