package me.mat.jprocessor.transformer;

import me.mat.jprocessor.jar.memory.MemoryClass;
import me.mat.jprocessor.jar.memory.MemoryInstructions;
import me.mat.jprocessor.jar.memory.MemoryMethod;
import org.objectweb.asm.tree.AbstractInsnNode;

public interface MethodTransformer {

    void transform(MemoryClass memoryClass, MemoryMethod memoryMethod);

    void transform(MemoryClass memoryClass, MemoryMethod memoryMethod,
                   MemoryInstructions instructions, AbstractInsnNode instruction);

}
