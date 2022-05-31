package me.mat.jprocessor.mappings.mapping.processor.impl;

import me.mat.jprocessor.jar.clazz.MemoryClass;
import me.mat.jprocessor.jar.clazz.MemoryMethod;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.mapping.FieldMapping;
import me.mat.jprocessor.mappings.mapping.Mapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;
import me.mat.jprocessor.mappings.mapping.processor.MappingProcessor;
import me.mat.jprocessor.util.asm.ASMUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ProGuardProcessor implements MappingProcessor {

    private final Map<MemoryMethod, Map<String, Integer>> localVariableCounters = new HashMap<>();

    private static final String TAB = "    ";
    private static final String DELIMITER = " -> ";

    private MappingManager mappingManager;

    private Map<String, Mapping> classMappings;
    private Map<String, Mapping> reverseClassMappings;
    private Map<String, List<FieldMapping>> fieldMappings;
    private Map<String, List<MethodMapping>> methodMappings;

    @Override
    public void process(String line) {
        if (line.startsWith("#")) {
            return;
        }

        line = line.replaceAll("\\.", "/");
        if (!line.startsWith(TAB)) {
            String[] data = line.split(DELIMITER);
            mappingManager.mapClass(data[0], data[1].substring(0, data[1].length() - 1));
        } else {
            line = line.substring(TAB.length());
            if (line.contains(":")) {
                line = line.substring(line.lastIndexOf(":") + 1);
            }
            String[] data = line.split(DELIMITER);
            String[] subData = data[0].split(" ");
            if (!line.contains("(")) {
                mappingManager.mapField(subData[1], data[1], subData[0]);
            } else {
                mappingManager.mapMethod(
                        subData[1].substring(0, subData[1].indexOf("(")),
                        data[1],
                        subData[0],
                        subData[1].substring(subData[1].indexOf("(") + 1, subData[1].indexOf(")"))
                );
            }
        }
    }

    @Override
    public void build(Map<String, Mapping> classMappings, Map<String, Mapping> reverseClassMappings,
                      Map<String, List<FieldMapping>> fieldMappings, Map<String, List<MethodMapping>> methodMappings) {
        // save the mappings
        this.classMappings = classMappings;
        this.reverseClassMappings = reverseClassMappings;
        this.fieldMappings = fieldMappings;
        this.methodMappings = methodMappings;

        // map all the types and descriptions
        mapReturnTypes();
        mapDescriptions();

        // build all the descriptions
        methodMappings.forEach((className, mappings) -> mappings.forEach(methodMapping -> {
            // build the unmapped version of the description
            methodMapping.description = buildDescription(methodMapping.description, methodMapping.returnType, false);

            // build the mapped version of the description
            methodMapping.mappedDescription = buildDescription(methodMapping.mappedDescription, methodMapping.mappedReturnType, true);
        }));
    }

    @Override
    public void buildLocalVariables(MemoryClass memoryClass, List<MemoryMethod> methods) {
        // loop through all the methods
        methods.forEach(memoryMethod -> {

            // define a counter for each method
            AtomicReference<Integer> counter = new AtomicReference<>(0);

            // loop through all the local variables and generate the variable names
            memoryMethod.localVariables.forEach(localVariable
                    -> localVariable.setName("var" + counter.getAndSet(counter.get() + 1)));
        });
    }

    @Override
    public void manager(MappingManager mappingManager) {
        this.mappingManager = mappingManager;
    }

    /**
     * Maps all the return types
     */

    void mapReturnTypes() {
        fieldMappings.forEach((className, mappings) -> mappings.forEach(fieldMapping -> {
            fieldMapping.returnType = ASMUtil.toByteCodeFromJava(fieldMapping.returnType);
            fieldMapping.mappedReturnType = getMappedType(fieldMapping.returnType);
        }));
        methodMappings.forEach((className, mappings) -> mappings.forEach(methodMapping -> {
            methodMapping.returnType = ASMUtil.toByteCodeFromJava(methodMapping.returnType);
            methodMapping.mappedReturnType = getMappedType(methodMapping.returnType);
        }));
    }

    /**
     * Maps all the descriptions
     */

    void mapDescriptions() {
        methodMappings.forEach((className, mappings) -> mappings.forEach(methodMapping -> {
            if (!methodMapping.description.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (String type : methodMapping.description.split(",")) {
                    builder.append(getMappedType(ASMUtil.toByteCodeFromJava(type)));
                    builder.append(",");
                }
                String description = builder.toString();
                if (!description.isEmpty()) {
                    methodMapping.mappedDescription = description.substring(
                            0,
                            description.length() - 1
                    );
                }
            }
        }));
    }

    /**
     * Builds a method description based on the
     * unmapped version of the description
     * and the provided return type
     *
     * @param description unmapped version of the description
     * @param returnType  return type of the method
     * @param mapped      flag containing if it's a name or mapping
     * @return {@link String}
     */

    String buildDescription(String description, String returnType, boolean mapped) {
        StringBuilder builder = new StringBuilder("(");
        for (String type : description.split(",")) {
            builder.append(mapped ? type : ASMUtil.toByteCodeFromJava(type));
        }
        builder.append(")");
        builder.append(returnType);
        return builder.toString();
    }

    /**
     * Gets a mapped type from a type
     *
     * @param name type that you want to get the mapping for
     * @return {@link String}
     */

    String getMappedType(String name) {
        StringBuilder builder = new StringBuilder();
        boolean complex = false;
        String type = name;
        if (type.startsWith("[")) {
            int offset = type.lastIndexOf("[") + 1;
            type = type.substring(offset);
            for (int i = 0; i < offset; i++) {
                builder.append("[");
            }
            if (type.startsWith("L")) {
                type = type.substring(1, type.length() - 1);
                builder.append("L");
                complex = true;
            }
        } else if (type.startsWith("L")) {
            type = type.substring(1, type.length() - 1);
            builder.append("L");
            complex = true;
        }

        String mapping;
        if (classMappings.containsKey(type)) {
            mapping = classMappings.get(type).mapping;
        } else if (reverseClassMappings.containsKey(type)) {
            mapping = reverseClassMappings.get(type).mapping;
        } else {
            mapping = type;
        }

        builder.append(mapping);
        if (complex) {
            builder.append(";");
        }
        return builder.toString();
    }

}
