package me.mat.jprocessor.mappings.generation.generator;

import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.mappings.MappingManager;

public interface MappingGenerator {

    void generate(String className, MemoryClass memoryClass);

    void manager(MappingManager mappingManager);

}
