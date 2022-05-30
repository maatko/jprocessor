package me.mat.jprocessor.transformer;

import me.mat.jprocessor.jar.clazz.MemoryClass;
import me.mat.jprocessor.jar.clazz.MemoryField;

public interface FieldTransformer {

    void transform(MemoryClass memoryClass, MemoryField memoryField);

}
