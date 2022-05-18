package me.mat.jprocessor.mappings.processor;

import me.mat.jprocessor.mappings.MappingManager;

public interface MappingProcessor {

    void process(String line);

    void manager(MappingManager mappingManager);

}
