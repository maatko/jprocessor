package me.mat.jprocessor.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.jar.MemoryJar;
import me.mat.jprocessor.jar.cls.MemoryMethod;
import me.mat.jprocessor.mappings.generation.MappingGenerateException;
import me.mat.jprocessor.mappings.generation.generator.MappingGenerator;
import me.mat.jprocessor.mappings.mapping.FieldMapping;
import me.mat.jprocessor.mappings.mapping.Mapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;
import me.mat.jprocessor.mappings.mapping.processor.MappingProcessor;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MappingManager extends SimpleRemapper {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private final Map<String, Mapping> classMappings = new HashMap<>();

    private final Map<String, Mapping> reverseClassMappings = new HashMap<>();

    private final Map<String, List<FieldMapping>> fieldMappings = new HashMap<>();

    private final Map<String, List<MethodMapping>> methodMappings = new HashMap<>();

    private final Map<String, String> mappings;

    private final boolean unMapping;

    private String currentClass;

    public MappingManager(MappingProcessor processor, File mappings, MemoryJar memoryJar) throws MappingLoadException {
        super(new HashMap<>());

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

        // fix all the method overrides
        memoryJar.getClasses().forEach((className, memoryClass) -> {
            Mapping classMapping = reverseClassMappings.get(className);
            if (classMapping != null) {
                currentClass = classMapping.name;
                memoryClass.methods.stream().filter(MemoryMethod::isOverride).forEach(mm -> {
                    Mapping superClassMapping = reverseClassMappings.get(mm.baseClass.name());
                    MethodMapping methodMapping = getMethodByMapping(
                            superClassMapping.name,
                            mm.baseMethod.name(),
                            mm.baseMethod.description()
                    );
                    if (methodMapping != null) {
                        mapMethod(
                                methodMapping.name,
                                methodMapping.mapping,
                                methodMapping.returnType,
                                methodMapping.mappedReturnType,
                                methodMapping.description,
                                methodMapping.mappedDescription
                        );
                    }
                });
            }
        });

        // set the unMapping flag to false
        this.unMapping = true;

        // get the mappings
        this.mappings = getMappings();
    }

    public MappingManager(MappingGenerator mappingGenerator, MemoryJar memoryJar) throws MappingGenerateException {
        super(new HashMap<>());

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

        // get the mappings
        this.mappings = getMappings();
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
     * Gets a class mapping by the name of the class
     *
     * @param name name of the class that you want to retrive
     * @return {@link Mapping}
     */

    public Mapping getClass(String name) {
        if (classMappings.containsKey(name)) {
            return classMappings.get(name);
        }
        return reverseClassMappings.get(name);
    }

    /**
     * Gets a field mapping by the class and return type
     *
     * @param className  name of the class that the method is in
     * @param name       name of the field that you want to get the mapping for
     * @param returnType return type of the field that you want to get the mapping for
     * @return {@link MethodMapping}
     */

    public FieldMapping getField(String className, String name, String returnType) {
        return fieldMappings.getOrDefault(className, new ArrayList<>()).stream().filter(fm -> fm.name.equals(name) && fm.returnType.equals(returnType)).findFirst().orElse(null);
    }

    /**
     * Gets a field mapping by the class and return type
     *
     * @param className        name of the class that the method is in
     * @param mapping          mapping of the field that you want to get the mapping for
     * @param mappedReturnType mapped return type of the field that you want to get the mapping for
     * @return {@link MethodMapping}
     */

    public FieldMapping getFieldByMapping(String className, String mapping, String mappedReturnType) {
        return fieldMappings.getOrDefault(className, new ArrayList<>()).stream().filter(fm -> fm.mapping.equals(mapping) && fm.mappedReturnType.equals(mappedReturnType)).findFirst().orElse(null);
    }

    /**
     * Gets a field mapping by the class and return type
     *
     * @param className  name of the class that the method is in
     * @param mapping    mapping of the field that you want to get the mapping for
     * @param returnType return type of the field that you want to get the mapping for
     * @return {@link MethodMapping}
     */

    public FieldMapping getFieldCustom(String className, String mapping, String returnType) {
        return fieldMappings.getOrDefault(className, new ArrayList<>()).stream().filter(fm -> fm.mapping.equals(mapping) && fm.returnType.equals(returnType)).findFirst().orElse(null);
    }

    /**
     * Gets a method mapping by the class, name and description
     *
     * @param className   name of the class that the method is in
     * @param name        name of the method that you want to get the mapping for
     * @param description description of the method that you want to get the mapping for
     * @return {@link MethodMapping}
     */

    public MethodMapping getMethod(String className, String name, String description) {
        return methodMappings.getOrDefault(className, new ArrayList<>()).stream().filter(mm -> mm.name.equals(name) && mm.description.equals(description)).findFirst().orElse(null);
    }

    /**
     * Gets a field mapping by the class and return type
     *
     * @param className         name of the class that the method is in
     * @param mapping           mapping of the method that you want to get the mapping for
     * @param mappedDescription mapped description  of the method that you want to get the mapping for
     * @return {@link MethodMapping}
     */

    public MethodMapping getMethodByMapping(String className, String mapping, String mappedDescription) {
        return methodMappings.getOrDefault(className, new ArrayList<>()).stream().filter(mm -> mm.mapping.equals(mapping) && mm.mappedDescription.equals(mappedDescription)).findFirst().orElse(null);
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
        /*
        methodMappings.forEach((className, methodMappings) -> {
            Mapping classMapping = getClass(className);
            if (classMapping != null) {
                if (unMapping) {
                    methodMappings.forEach(mapping -> mappings.put(
                            classMapping.mapping + "." + mapping.mapping + mapping.mappedDescription,
                            mapping.name
                    ));
                } else {
                    methodMappings.forEach(mapping -> mappings.put(
                            classMapping.name + "." + mapping.name + mapping.description,
                            mapping.mapping
                    ));
                }
            }
        });*/
        return mappings;
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        if (!desc.startsWith("(")) {
            return mapFieldName(owner, name, desc);
        }
        return super.mapMethodName(owner, name, desc);
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        return super.mapFieldName(owner, name, desc);
    }

    @Override
    public String mapRecordComponentName(String owner, String name, String descriptor) {
        return mapFieldName(owner, name, descriptor);
    }

    @Override
    public String mapMethodDesc(String methodDescriptor) {
        return super.mapMethodDesc(methodDescriptor);
    }

    @Override
    public Object mapValue(Object value) {
        return super.mapValue(value);
    }

    @Override
    public String map(String key) {
        return this.mappings.get(key);
    }

}
