package natureoverhaul;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;

/**
 * Created by Olivier on 22/05/2014.
 */
public final class TreeData {
    public enum Component{
        SAPLING,TRUNK,LEAF
    }
    private static HashSet<TreeData> trees = new HashSet<TreeData>();
    private final Block sapling, log, leaves;
    private final int sapMeta, logMeta, leafMeta;
    public TreeData(Block sapling, Block log, Block leaves, int sapMeta, int logMeta, int leafMeta){
        this.sapling = sapling;
        this.log = log;
        this.leaves = leaves;
        this.sapMeta = sapMeta;
        this.logMeta = logMeta;
        this.leafMeta = leafMeta;
    }

    public TreeData(NBTTagCompound compound){
        NBTTagCompound tag = compound.getCompoundTag("Sapling");
        this.sapling = Block.getBlockFromName(tag.getString("Block"));
        this.sapMeta = tag.getInteger("Metadata");
        tag = compound.getCompoundTag("Trunk");
        this.log = Block.getBlockFromName(tag.getString("Block"));
        this.logMeta = tag.getInteger("Metadata");
        tag = compound.getCompoundTag("Leaf");
        this.leaves = Block.getBlockFromName(tag.getString("Block"));
        this.leafMeta = tag.getInteger("Metadata");
    }

    public boolean isValid(){
        return sapling!=null && log!=null && leaves!=null;
    }

    public void register(){
        trees.add(this);
    }

    public static TreeData getTree(Block block, int meta, Component component){
        for(TreeData data:trees){
            if(data.getBlock(component) == block && data.getMeta(component) == meta){
                return data;
            }
        }
        return null;
    }

    public Block getBlock(Component component){
        switch (component){
            case SAPLING:
                return sapling;
            case TRUNK:
                return log;
            case LEAF:
                return leaves;
        }
        return null;
    }

    public int getMeta(Component component){
        switch (component){
            case SAPLING:
                return sapMeta;
            case TRUNK:
                return logMeta;
            case LEAF:
                return leafMeta;
        }
        return -1;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder().append(sapling).append(log).append(leaves).append(sapMeta).append(logMeta).append(leafMeta).toHashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj==this){
            return true;
        }
        if(obj==null){
            return false;
        }
        if(obj instanceof TreeData){
            for(Component component:Component.values()){
                if(((TreeData) obj).getBlock(component)!=this.getBlock(component) || ((TreeData) obj).getMeta(component)!=this.getMeta(component))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString(){
        StringBuilder value = new StringBuilder();
        for(Component c:Component.values()){
            value.append(GameData.getBlockRegistry().getNameForObject(getBlock(c))).append("(").append(getMeta(c)).append(")-");
        }
        value.deleteCharAt(value.length()-1);
        return value.toString();
    }
}
