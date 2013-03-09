package natureoverhaul;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;
import cpw.mods.fml.relauncher.IClassTransformer;

public class NOTransformer extends AccessTransformer{
	private static NOTransformer instance;
    private static List mapFiles = new LinkedList();
    
	public NOTransformer() throws IOException {
		super();
		instance = this;
		// TODO Auto-generated constructor stub
	}

	//Class names
	private final String[] classNamePlantBlockObfusc ={ "ajg","aje","akt","ale","alh","aly","ama","ami"}; // 1.4.7 obfuscation    
    private final String[] classNamePlantBlock = 
    	{"net.minecraft.block.BlockCactus","net.minecraft.block.BlockFlower",
    		"net.minecraft.block.BlockLeaves","net.minecraft.block.BlockMushroom",
    		"net.minecraft.block.BlockNetherStalk","net.minecraft.block.BlockReed",
    		"net.minecraft.block.BlockSapling","net.minecraft.block.BlockStem"};
    private final String[] classNamePlantBlockJava = 
    	{"net/minecraft/block/BlockCactus","net/minecraft/block/BlockFlower",
    		"net/minecraft/block/BlockLeaves","net/minecraft/block/BlockMushroom",
    		"net/minecraft/block/BlockNetherStalk","net/minecraft/block/BlockReed",
    		"net/minecraft/block/BlockSapling","net/minecraft/block/BlockStem"};
    private final String classNameWorldObfusc = "yc";
    private final String classNameWorld = "net/minecraft/world/World";
    //Method names
    private final String BlockMethodName="updateTick";//func_71847_b
    private final String[] obfBlockMethodNames={"b_","c","b","c","d","c","c","b"};//TODO:Find method name
    private String obfBlockMethodName;
    private String descriptor;//TODO:Find method descriptor (yc=net/minecraft/world/World)
	@Override
	public byte[] transform(String name, byte[] bytes) {
		for(String current : classNamePlantBlockObfusc)
		if (name.equals(current))
        {
            return handlePlantsTransform(bytes, name, true);
        }
		for(String current : classNamePlantBlock)
        if (name.equals(current))
        {
            return handlePlantsTransform(bytes, name.split("\\.")[3], false);
        }      
        return bytes;
	}

	private byte[] handlePlantsTransform(byte[] bytes, String name, boolean isObfuscated) {
		// TODO 
		descriptor="(L"+(isObfuscated?classNameWorldObfusc:classNameWorld)+";IIILjava/util/Random;)V";
		System.out.println(name);
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
     // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();
        if (isObfuscated){
        	for(int k=0;k<classNamePlantBlockObfusc.length;k++)
        		if (name.equals(classNamePlantBlockObfusc[k]))
                {
        			obfBlockMethodName=obfBlockMethodNames[k];
                }
        }
        while(methods.hasNext())
        {
            MethodNode m = methods.next();
            if (m.name.equals( isObfuscated ? obfBlockMethodName : BlockMethodName) && m.desc.equals(descriptor))
            {
            	System.out.println("Patching method");
                
            	  AbstractInsnNode targetNode = null;
               Iterator iter = m.instructions.iterator();
                while (iter.hasNext())
                {
                    targetNode = (AbstractInsnNode) iter.next();
                    /*if (targetNode.getOpcode() != ISTORE)
                    {
                        iter.remove();
                    }
                    else
                    {
                        // leave the ISTORE node
                        break;
                    }*/
                }           
                if (targetNode == null)
                {
                    System.err.println("Transformer did not run correctly!");
                    return bytes;
                }
                
                // instruction list
                InsnList toInject = new InsnList();
                //argument mapping[world,x,y,z,random]
                toInject.add(new VarInsnNode(ALOAD, 1));
                toInject.add(new VarInsnNode(ILOAD, 2));
                toInject.add(new VarInsnNode(ILOAD, 3));
                toInject.add(new VarInsnNode(ILOAD, 4));
                toInject.add(new VarInsnNode(ALOAD, 5));
                toInject.add(new MethodInsnNode(INVOKEVIRTUAL,"natureoverhaul/NatureOverhaul","updateTick",descriptor));
                // inject instruction list into method
                m.instructions.insert(targetNode, toInject);                
                System.out.println("Patching done");
                break;
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
	}

}
