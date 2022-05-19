package me.mat.jprocessor.mappings.mapping.processor.impl;

import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.mapping.FieldMapping;
import me.mat.jprocessor.mappings.mapping.Mapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;
import me.mat.jprocessor.mappings.mapping.processor.MappingProcessor;
import me.mat.jprocessor.util.asm.ASMUtil;

import java.util.List;
import java.util.Map;

public class ProGuardProcessor implements MappingProcessor {

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
            methodMapping.description = buildDescription(methodMapping.description, methodMapping.returnType);

            // build the mapped version of the description
            methodMapping.mappedDescription = buildDescription(methodMapping.mappedDescription, methodMapping.mappedReturnType);
        }));
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
            String returnType = fieldMapping.returnType;
            fieldMapping.returnType = ASMUtil.toByteCodeFromJava(returnType);
            fieldMapping.mappedReturnType = ASMUtil.toByteCodeFromJava(getMappedType(returnType));
        }));
        methodMappings.forEach((className, mappings) -> mappings.forEach(methodMapping -> {
            String returnType = methodMapping.returnType;
            methodMapping.returnType = ASMUtil.toByteCodeFromJava(returnType);
            methodMapping.mappedReturnType = ASMUtil.toByteCodeFromJava(getMappedType(returnType));
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
                    builder.append(getMappedType(type));
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
     * @return {@link String}
     */

    String buildDescription(String description, String returnType) {
        StringBuilder builder = new StringBuilder("(");
        for (String type : description.split(",")) {
            builder.append(ASMUtil.toByteCodeFromJava(type));
        }
        builder.append(")");
        builder.append(returnType);
        return builder.toString();
    }

    /**
     * Gets a mapped type from a type
     *
     * @param type type that you want to get the mapping for
     * @return {@link String}
     */

    String getMappedType(String type) {
        if (reverseClassMappings.containsKey(type)) {
            return reverseClassMappings.get(type).mapping;
        }
        return type;
    }

}
