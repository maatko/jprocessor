package me.mat.jprocessor.transformer;

import me.mat.jprocessor.jar.clazz.MemoryClass;

public interface ClassTransformer extends FieldTransformer, MethodTransformer {

    void transform(MemoryClass memoryClass);


}
