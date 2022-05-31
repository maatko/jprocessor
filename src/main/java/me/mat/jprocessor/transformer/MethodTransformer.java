package me.mat.jprocessor.transformer;

import me.mat.jprocessor.jar.clazz.MemoryClass;
import me.mat.jprocessor.jar.clazz.MemoryInstructions;
import me.mat.jprocessor.jar.clazz.MemoryMethod;
import org.objectweb.asm.tree.AbstractInsnNode;

public interface MethodTransformer {

    void transform(MemoryClass memoryClass, MemoryMethod memoryMethod);

    void transform(MemoryClass memoryClass, MemoryMethod memoryMethod,
                   MemoryInstructions instructions, AbstractInsnNode instruction);

}
