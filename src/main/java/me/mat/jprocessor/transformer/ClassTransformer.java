package me.mat.jprocessor.transformer;

import me.mat.jprocessor.jar.cls.MemoryClass;

public interface ClassTransformer extends FieldTransformer, MethodTransformer {

    void transform(MemoryClass memoryClass);


}
