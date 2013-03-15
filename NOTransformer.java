package natureoverhaul;

import static org.objectweb.asm.ClassWriter.*;
import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;

public class NOTransformer extends AccessTransformer{
	private static NOTransformer instance;
    private static List mapFiles = new LinkedList();
    
	public NOTransformer() throws IOException {
		super();
		instance = this;
		// classloader exclusions
       /* addClassLoaderExclusion("java.");
        addClassLoaderExclusion("sun.");
        addClassLoaderExclusion("org.lwjgl.");
        addClassLoaderExclusion("cpw.mods.fml.relauncher.");
        addClassLoaderExclusion("net.minecraftforge.classloading.");

        // transformer exclusions
        addTransformerExclusion("javax.");
        addTransformerExclusion("org.objectweb.asm.");
        addTransformerExclusion("com.google.common.");*/
	}

	//Class names
	private final String[] classNamePlantBlockObfusc ={ "ajg",/*"aje","akt","ale",*/"alh","aly"/*,"ama","ami"*/}; // 1.4.7 obfuscation    
    private final String[] classNamePlantBlock = //removed classes extending blockflower, which will be treated separately
    	{"net.minecraft.block.BlockCactus",/*"net.minecraft.block.BlockFlower",
    		"net.minecraft.block.BlockLeaves","net.minecraft.block.BlockMushroom",*/
    		"net.minecraft.block.BlockNetherStalk","net.minecraft.block.BlockReed"/*,
    		"net.minecraft.block.BlockSapling","net.minecraft.block.BlockStem"*/};
    private final String classNameWorldObfusc = "yc";
    private final String classNameWorld = "net/minecraft/world/World";
    //Method names
    private final String BlockMethodName="updateTick";//func_71847_b
    private final String[] obfBlockMethodNames={"b_",/*"c","b","c",*/"d","c"/*,"c","b"*/};//TODO:Find method name
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
		if (isObfuscated){
        	for(int k=0;k<classNamePlantBlockObfusc.length;k++)
        		if (name.equals(classNamePlantBlockObfusc[k]))
                {
        			obfBlockMethodName=obfBlockMethodNames[k];
                }
		}
		descriptor="(L"+(isObfuscated?classNameWorldObfusc:classNameWorld)+";IIILjava/util/Random;)V";
		System.out.println(name);
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        /*MethodNode methodnode = (MethodNode) classNode.visitMethod(ACC_PUBLIC, "updateTick", descriptor, null, null);
        methodnode.instructions.add(new VarInsnNode(ALOAD, 1));
        methodnode.instructions.add(new FieldInsnNode(PUTSTATIC, "natureoverhaul/NatureOverhaul", "tickedWorld", Type.getObjectType("net/minecraft/world/World").getDescriptor())); 
        methodnode.instructions.add(new VarInsnNode(ILOAD, 2));
        methodnode.instructions.add(new FieldInsnNode(PUTSTATIC, "natureoverhaul/NatureOverhaul", "tickX", "I")); 
        methodnode.instructions.add(new VarInsnNode(ILOAD, 3));
        methodnode.instructions.add(new FieldInsnNode(PUTSTATIC, "natureoverhaul/NatureOverhaul", "tickY", "I")); 
        methodnode.instructions.add(new VarInsnNode(ILOAD, 4));
        methodnode.instructions.add(new FieldInsnNode(PUTSTATIC, "natureoverhaul/NatureOverhaul", "tickZ", "I")); 
        methodnode.instructions.add(new VarInsnNode(ALOAD, 5));
        methodnode.instructions.add(new FieldInsnNode(PUTSTATIC, "natureoverhaul/NatureOverhaul", "tickRand", Type.getObjectType("java/util/Random").getDescriptor())); 
        methodnode.instructions.add(new InsnNode(RETURN));*/
        
     // find method to inject into
        Iterator<MethodNode> methods = classNode.methods.iterator();  
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
                	if (((AbstractInsnNode) iter.next()).getOpcode() == 177)
                    {
                        break;
                    } 
                    targetNode = (AbstractInsnNode) iter.next();
                    System.out.println(targetNode.toString()+","+targetNode.getOpcode());
                        
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
                //toInject.add(new FieldInsnNode(PUTFIELD, "natureoverhaul/NatureOverhaul","onUpdateTick",Type.getObjectType("net/minecraft/world/World").getDescriptor()));
                toInject.add(new VarInsnNode(ILOAD, 2));
                //toInject.add(new FieldInsnNode(PUTFIELD, "natureoverhaul/NatureOverhaul","onUpdateTick","I"));
                toInject.add(new VarInsnNode(ILOAD, 3));
                //toInject.add(new FieldInsnNode(PUTFIELD, "natureoverhaul/NatureOverhaul","onUpdateTick","I"));              
                toInject.add(new VarInsnNode(ILOAD, 4));
                //toInject.add(new FieldInsnNode(PUTFIELD, "natureoverhaul/NatureOverhaul","onUpdateTick","I"));              
                toInject.add(new VarInsnNode(ALOAD, 5));
                //toInject.add(new FieldInsnNode(PUTFIELD, "natureoverhaul/NatureOverhaul","onUpdateTick",Type.getObjectType("java/util/Random").getDescriptor()));             
                toInject.add(new MethodInsnNode(INVOKEVIRTUAL,"natureoverhaul/NatureOverhaul","onUpdateTick",descriptor));
                //toInject.add(new InsnNode(RETURN));
                // inject instruction list into method
                m.instructions.insertBefore(targetNode, toInject);                
                System.out.println("Patching done");
                break;
            }
        }
        ClassWriter writer = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        classNode.accept(writer);
        System.out.println("Visiting class done");
        return writer.toByteArray();
	}

}
