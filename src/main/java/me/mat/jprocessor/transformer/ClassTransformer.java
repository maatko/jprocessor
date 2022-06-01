package me.mat.jprocessor.transformer;

import me.mat.jprocessor.jar.memory.MemoryClass;
import me.mat.jprocessor.jar.memory.MemoryField;
import me.mat.jprocessor.jar.memory.MemoryInstructions;
import me.mat.jprocessor.jar.memory.MemoryMethod;
import org.objectweb.asm.tree.AbstractInsnNode;

public interface ClassTransformer extends FieldTransformer, MethodTransformer {

    void transform(MemoryClass memoryClass);

    @Override
    default void transform(MemoryClass memoryClass, MemoryField memoryField) {
    }

    @Override
    default void transform(MemoryClass memoryClass, MemoryMethod memoryMethod) {
    }

    @Override
    default void transform(MemoryClass memoryClass, MemoryMethod memoryMethod,
                           MemoryInstructions instructions, AbstractInsnNode instruction) {
    }

}
