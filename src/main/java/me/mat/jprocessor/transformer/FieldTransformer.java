package me.mat.jprocessor.transformer;

import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.jar.cls.MemoryField;

public interface FieldTransformer {

    void transform(MemoryClass memoryClass, MemoryField memoryField);

}
