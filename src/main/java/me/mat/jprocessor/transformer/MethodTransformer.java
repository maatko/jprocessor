package me.mat.jprocessor.transformer;

import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.jar.cls.MemoryMethod;

public interface MethodTransformer {

    void transform(MemoryClass memoryClass, MemoryMethod memoryMethod);

}
