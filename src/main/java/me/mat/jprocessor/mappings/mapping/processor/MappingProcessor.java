package me.mat.jprocessor.mappings.mapping.processor;

import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.mapping.FieldMapping;
import me.mat.jprocessor.mappings.mapping.Mapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;

import java.util.List;
import java.util.Map;

public interface MappingProcessor {

    void process(String line);

    void build(Map<String, Mapping> classMappings, Map<String, Mapping> reverseClassMappings, Map<String, List<FieldMapping>> fieldMappings, Map<String, List<MethodMapping>> methodMappings);

    void manager(MappingManager mappingManager);

}
