package natureoverhaul;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.registry.GameData;
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
    private final IBlockState sapling, log, leaves;
    public TreeData(IBlockState sapling, IBlockState log, IBlockState leaves){
        this.sapling = sapling;
        this.log = log;
        this.leaves = leaves;
    }

    public TreeData(NBTTagCompound compound){
        NBTTagCompound tag = compound.getCompoundTag("Sapling");
        this.sapling = Block.getBlockFromName(tag.getString("Block")).getStateFromMeta(tag.getInteger("Metadata"));
        tag = compound.getCompoundTag("Trunk");
        this.log = Block.getBlockFromName(tag.getString("Block")).getStateFromMeta(tag.getInteger("Metadata"));
        tag = compound.getCompoundTag("Leaf");
        this.leaves = Block.getBlockFromName(tag.getString("Block")).getStateFromMeta(tag.getInteger("Metadata"));
    }

    public boolean isValid(){
        return sapling!=null && log!=null && leaves!=null;
    }

    public void register(){
        if(isValid())
            trees.add(this);
    }

    public static TreeData getTree(IBlockState block, Component component){
        for(TreeData data:trees){
            IBlockState temp = data.getState(component);
            if(Utils.equal(temp, block)){
                return data;
            }
        }
        return null;
    }

    public IBlockState getState(Component component){
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

    public Block getBlock(Component component){
        return getState(component).getBlock();
    }

    private int getMeta(Component component){
        IBlockState state = getState(component);
        if(state!=null)
            return state.getBlock().getMetaFromState(state);
        return -1;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder().append(sapling).append(log).append(leaves).toHashCode();
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
                if(!Utils.equal(((TreeData) obj).getState(component), this.getState(component)))
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
