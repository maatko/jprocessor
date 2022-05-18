package me.mat.jprocessor.mappings;

import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.mappings.mapping.FieldMapping;
import me.mat.jprocessor.mappings.mapping.Mapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;
import me.mat.jprocessor.mappings.processor.MappingProcessor;
import me.mat.jprocessor.util.asm.ASMUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingManager {

    private final Map<String, Mapping> classMappings = new HashMap<>();

    private final Map<String, Mapping> reverseClassMappings = new HashMap<>();

    private final Map<String, List<FieldMapping>> fieldMappings = new HashMap<>();

    private final Map<String, List<MethodMapping>> methodMappings = new HashMap<>();

    private String currentClass;

    public MappingManager(MappingProcessor processor, File mappings) throws MappingLoadException {
        // if the mappings file does not exist alert the user
        if (!mappings.exists()) {
            throw new MappingLoadException(mappings.getAbsolutePath() + " does not exist");
        }

        // log to console that mappings are being loaded
        JProcessor.Logging.info("Loading the mappings");

        // load all the mappings
        processor.manager(this);
        try (BufferedReader reader = new BufferedReader(new FileReader(mappings))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processor.process(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // construct the mapping data
        this.mapReturnTypes();
        this.mapDescriptions();

        // log the loaded data to the console
        JProcessor.Logging.info("Loaded '%d' class mappings", classMappings.size());
        JProcessor.Logging.info("Loaded '%d' field mappings", fieldMappings.size());
        JProcessor.Logging.info("Loaded '%d' method mappings", methodMappings.size());
    }

    public void mapClass(String name, String mapping) {
        currentClass = mapping;

        Mapping currentMapping = new Mapping(name, mapping);
        classMappings.put(mapping, currentMapping);
        reverseClassMappings.put(name, currentMapping);
    }

    public void mapField(String name, String mapping, String returnType) {
        List<FieldMapping> mappings = fieldMappings.getOrDefault(currentClass, new ArrayList<>());
        mappings.add(new FieldMapping(name, mapping, returnType));
        fieldMappings.put(currentClass, mappings);
    }

    public void mapMethod(String name, String mapping, String returnType, String description) {
        List<MethodMapping> mappings = methodMappings.getOrDefault(currentClass, new ArrayList<>());
        mappings.add(new MethodMapping(name, mapping, returnType, description));
        methodMappings.put(currentClass, mappings);
    }

    void mapReturnTypes() {
        fieldMappings.forEach((className, fieldMappings) -> fieldMappings.forEach(fieldMapping -> {
            String returnType = fieldMapping.returnType;
            if (reverseClassMappings.containsKey(returnType)) {
                fieldMapping.mappedReturnType = reverseClassMappings.get(returnType).mapping;
            }
            fieldMapping.returnType = ASMUtil.toByteCodeFromJava(fieldMapping.returnType);
            fieldMapping.mappedReturnType = ASMUtil.toByteCodeFromJava(fieldMapping.mappedReturnType);
        }));
        methodMappings.forEach((className, methodMappings) -> methodMappings.forEach(methodMapping -> {
            String returnType = methodMapping.returnType;
            if (reverseClassMappings.containsKey(returnType)) {
                methodMapping.mappedReturnType = reverseClassMappings.get(returnType).mapping;
            }
            methodMapping.returnType = ASMUtil.toByteCodeFromJava(methodMapping.returnType);
            methodMapping.mappedReturnType = ASMUtil.toByteCodeFromJava(methodMapping.mappedReturnType);
        }));
    }

    void mapDescriptions() {
        methodMappings.forEach((className, methodMappings) -> methodMappings.forEach(methodMapping -> {
            String description = methodMapping.description;
            if (!description.isEmpty()) {
                methodMapping.mappedDescription = "";
                String[] types = description.split(",");
                for (String type : types) {
                    if (reverseClassMappings.containsKey(type)) {
                        methodMapping.mappedDescription += reverseClassMappings.get(type).mapping;
                        methodMapping.mappedDescription += ",";
                    }
                }
                if (!methodMapping.mappedDescription.isEmpty()) {
                    methodMapping.mappedDescription = methodMapping.mappedDescription.substring(
                            0,
                            methodMapping.mappedDescription.length() - 1
                    );
                }
            }
        }));
    }

}
