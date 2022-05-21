package me.mat.jprocessor.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.jar.MemoryJar;
import me.mat.jprocessor.mappings.generation.MappingGenerateException;
import me.mat.jprocessor.mappings.generation.generator.MappingGenerator;
import me.mat.jprocessor.mappings.mapping.FieldMapping;
import me.mat.jprocessor.mappings.mapping.Mapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;
import me.mat.jprocessor.mappings.mapping.processor.MappingProcessor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MappingManager {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final Map<String, Mapping> classMappings = new HashMap<>();

    private final Map<String, Mapping> reverseClassMappings = new HashMap<>();

    private final Map<String, List<FieldMapping>> fieldMappings = new HashMap<>();

    private final Map<String, List<MethodMapping>> methodMappings = new HashMap<>();

    private final boolean unMapping;

    private String currentClass;

    public MappingManager(MappingProcessor processor, File mappings) throws MappingLoadException {
        // if the mappings file does not exist alert the user
        if (!mappings.exists()) {
            throw new MappingLoadException(mappings.getAbsolutePath() + " does not exist");
        }

        // log to console that mappings are being loaded
        JProcessor.Logging.info("Loading the mappings");

        // update the manager instance in the generator
        processor.manager(this);

        // loop through the mappings file and load the mappings
        try (BufferedReader reader = new BufferedReader(new FileReader(mappings))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processor.process(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // build the mapping data
        processor.build(classMappings, reverseClassMappings, fieldMappings, methodMappings);

        // log the loaded data to the console
        JProcessor.Logging.info("Loaded '%d' class mappings", classMappings.size());
        JProcessor.Logging.info("Loaded '%d' field mappings", fieldMappings.size());
        JProcessor.Logging.info("Loaded '%d' method mappings", methodMappings.size());

        // set the unMapping flag to false
        this.unMapping = true;
    }

    public MappingManager(MappingGenerator mappingGenerator, MemoryJar memoryJar) throws MappingGenerateException {
        // log to console that mappings are being loaded
        JProcessor.Logging.info("Generating mappings");

        // update the manager instance in the generator
        mappingGenerator.setMappingManager(this);

        // generate mappings for the provided jar
        mappingGenerator.map(memoryJar);

        // log the generated data to the console
        JProcessor.Logging.info("Generated '%d' class mappings", classMappings.size());
        JProcessor.Logging.info("Generated '%d' field mappings", fieldMappings.size());
        JProcessor.Logging.info("Generated '%d' method mappings", methodMappings.size());

        // set the unMapping flag to false
        this.unMapping = false;
    }

    public Mapping getClass(String name) {
        if (classMappings.containsKey(name)) {
            return classMappings.get(name);
        }
        return reverseClassMappings.get(name);
    }

    public MethodMapping getMethod(String className, String name, String description) {
        return methodMappings.getOrDefault(className, new ArrayList<>()).stream().filter(mm -> mm.name.equals(name) && mm.description.equals(description)).findFirst().orElse(null);
    }

    /**
     * Maps a class by its name and mapping
     *
     * @param name    name of the class
     * @param mapping mapping of the class
     */

    public void mapClass(String name, String mapping) {
        currentClass = name;

        if (!classMappings.containsKey(name)) {
            Mapping currentMapping = new Mapping(name, mapping);
            classMappings.put(name, currentMapping);
            reverseClassMappings.put(mapping, currentMapping);
        }
    }

    /**
     * Maps a field to the current class
     * by its name, mapping and return type
     *
     * @param name             name of the field that you want to map
     * @param mapping          mapping of the field that you want to map
     * @param returnType       return type of the field that you want to map
     * @param mappedReturnType mapped return type of the field that you want to map
     */

    public void mapField(String name, String mapping, String returnType, String mappedReturnType) {
        List<FieldMapping> mappings = fieldMappings.getOrDefault(currentClass, new ArrayList<>());
        mappings.add(new FieldMapping(name, mapping, returnType, mappedReturnType));
        fieldMappings.put(currentClass, mappings);
    }

    /**
     * Maps a field to the current class
     * by its name, mapping and return type
     *
     * @param name       name of the field that you want to map
     * @param mapping    mapping of the field that you want to map
     * @param returnType return type of the field that you want to map
     */

    public void mapField(String name, String mapping, String returnType) {
        mapField(name, mapping, returnType, returnType);
    }

    /**
     * Maps a method to the current class
     * by its name, mapping, return type and
     * the description
     *
     * @param name              name of the method that you want to map
     * @param mapping           mapping of the method that you want to map
     * @param returnType        return type of the method that you want to map
     * @param mappedReturnType  mapped return type of the method that you want to map
     * @param description       description of the method that you want to map
     * @param mappedDescription mapped description of the method that you want to map
     */

    public void mapMethod(String name, String mapping, String returnType, String mappedReturnType, String description, String mappedDescription) {
        List<MethodMapping> mappings = methodMappings.getOrDefault(currentClass, new ArrayList<>());
        mappings.add(new MethodMapping(name, mapping, returnType, mappedReturnType, description, mappedDescription));
        methodMappings.put(currentClass, mappings);
    }

    /**
     * Maps a method to the current class
     * by its name, mapping, return type and
     * the description
     *
     * @param name        name of the method that you want to map
     * @param mapping     mapping of the method that you want to map
     * @param returnType  return type of the method that you want to map
     * @param description description of the method that you want to map
     */

    public void mapMethod(String name, String mapping, String returnType, String description) {
        mapMethod(name, mapping, returnType, returnType, description, description);
    }

    /**
     * Saves all the mappings into a json file
     *
     * @param file file that you want to save tov
     */

    public void save(File file) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            JsonArray array = new JsonArray();

            classMappings.forEach((className, mapping) -> {
                JsonObject object = mapping.toJson();

                List<FieldMapping> fields = fieldMappings.getOrDefault(className, null);
                if (fields != null) {
                    JsonArray fieldsArray = new JsonArray();
                    fields.forEach(fieldMapping -> fieldsArray.add(fieldMapping.toJson()));
                    object.add("fields", fieldsArray);
                }

                List<MethodMapping> methods = methodMappings.getOrDefault(className, null);
                if (methods != null) {
                    JsonArray methodsArray = new JsonArray();
                    methods.forEach(methodMapping -> methodsArray.add(methodMapping.toJson()));
                    object.add("methods", methodsArray);
                }

                array.add(object);
            });

            fileWriter.write(GSON.toJson(array));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Compiled all the mappings that
     * the SimpleRemapper supports
     *
     * @return {@link Map}
     */

    public Map<String, String> getMappings() {
        Map<String, String> mappings = new HashMap<>();
        classMappings.forEach((className, mapping) -> mappings.put(
                unMapping ? mapping.mapping : mapping.name,
                unMapping ? mapping.name : mapping.mapping
        ));
        fieldMappings.forEach((className, fieldMappings) -> fieldMappings.forEach(mapping -> {
            Mapping classMapping = getClass(className);
            if (classMapping != null) {
                mappings.put(
                        (unMapping ? classMapping.mapping : classMapping.name) + "." + (unMapping ? mapping.mapping : mapping.name),
                        unMapping ? mapping.name : mapping.mapping
                );
            }
        }));
        methodMappings.forEach((className, methodMappings) -> {
            Mapping classMapping = getClass(className);
            if (classMapping != null) {
                methodMappings.forEach(mapping -> {
                    mappings.put(
                            (unMapping ? classMapping.mapping : classMapping.name) + "." + (unMapping ? mapping.mapping + mapping.mappedDescription : mapping.name + mapping.description),
                            unMapping ? mapping.name : mapping.mapping
                    );
                });
            }
        });
        return mappings;
    }

}
