package me.mat.jprocessor.transformer;

import me.mat.jprocessor.jar.clazz.MemoryClass;
import me.mat.jprocessor.jar.clazz.MemoryMethod;

public interface MethodTransformer {

    void transform(MemoryClass memoryClass, MemoryMethod memoryMethod);

}
