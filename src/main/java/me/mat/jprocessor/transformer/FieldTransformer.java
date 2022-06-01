package me.mat.jprocessor.transformer;

import me.mat.jprocessor.jar.memory.MemoryClass;
import me.mat.jprocessor.jar.memory.MemoryField;

public interface FieldTransformer {

    void transform(MemoryClass memoryClass, MemoryField memoryField);

}
