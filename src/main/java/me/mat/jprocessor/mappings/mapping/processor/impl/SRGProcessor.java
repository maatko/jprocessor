package me.mat.jprocessor.mappings.mapping.processor.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.jar.memory.MemoryClass;
import me.mat.jprocessor.jar.memory.MemoryMethod;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.mapping.FieldMapping;
import me.mat.jprocessor.mappings.mapping.Mapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;
import me.mat.jprocessor.mappings.mapping.processor.MappingProcessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SRGProcessor implements MappingProcessor {

    private MappingManager mappingManager;

    @Override
    public void process(String line) {
        Type type = Type.getType(line);
        if (type == null) {
            throw new RuntimeException("Invalid type for line: " + line);
        }
        String[] data = type.getData(line);
        switch (type) {
            case PACKAGE:
                break;
            case CLASS:
                mappingManager.mapClass(data[1], data[0]);
                break;
            case FIELD:
                String className = data[0].substring(0, data[0].lastIndexOf("/"));
                mappingManager.mapClass(className, className);

                String fieldName = data[0].substring(data[0].lastIndexOf("/") + 1);
                String fieldMapping = data[1].substring(data[1].lastIndexOf("/") + 1);

                mappingManager.mapField(fieldName, fieldMapping, "");
                break;
            case METHOD:
                className = data[0].substring(0, data[0].lastIndexOf("/"));
                mappingManager.mapClass(className, className);

                String methodName = data[0].substring(data[0].lastIndexOf("/") + 1);
                String description = data[1];
                String methodMapping = data[2].substring(data[2].lastIndexOf("/") + 1);
                String mappedDescription = data[3];

                mappingManager.mapMethod(methodName, methodMapping, "", "", description, mappedDescription);
                break;
            default:
                JProcessor.Logging.warn("'%s' is not a valid mapping entry", line);
        }
    }

    @Override
    public void build(Map<String, Mapping> classMappings, Map<String, Mapping> reverseClassMappings, Map<String, List<FieldMapping>> fieldMappings, Map<String, List<MethodMapping>> methodMappings) {

    }

    @Override
    public void buildLocalVariables(MemoryClass memoryClass, List<MemoryMethod> methods) {
        // loop through all the methods
        methods.forEach(memoryMethod -> {

            // define a counter for each method
            AtomicReference<Integer> counter = new AtomicReference<>(0);

            // loop through all the local variables and generate the variable names
            memoryMethod.localVariables.forEach(localVariable -> {

                // if the local variable name is not a this
                if (!localVariable.name().equals("this")) {

                    // generate a new name
                    localVariable.setName("var" + counter.getAndSet(counter.get() + 1));
                }
            });
        });
    }

    @Override
    public void manager(MappingManager mappingManager) {
        this.mappingManager = mappingManager;
    }

    @RequiredArgsConstructor
    private enum Type {

        PACKAGE("PK"),
        CLASS("CL"),
        FIELD("FD"),
        METHOD("MD");

        @NonNull
        final String prefix;

        public String[] getData(String line) {
            return line.substring(toString().length()).split(" ");
        }

        public static Type getType(String line) {
            for (Type type : values()) {
                if (line.startsWith(type.toString())) {
                    return type;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return prefix + ": ";
        }

    }

}
