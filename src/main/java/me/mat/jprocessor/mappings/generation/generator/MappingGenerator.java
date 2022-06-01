package me.mat.jprocessor.mappings.generation.generator;

import lombok.Setter;
import me.mat.jprocessor.jar.memory.MemoryJar;
import me.mat.jprocessor.jar.memory.MemoryClass;
import me.mat.jprocessor.jar.memory.MemoryField;
import me.mat.jprocessor.jar.memory.MemoryLocalVariable;
import me.mat.jprocessor.jar.memory.MemoryMethod;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.mapping.Mapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;

import java.util.Map;

public abstract class MappingGenerator {

    @Setter
    protected MappingManager mappingManager;

    public abstract String mapClass(String className, MemoryClass memoryClass);

    public abstract String mapField(String className, MemoryClass memoryClass, MemoryField memoryField);

    public abstract String mapMethod(String className, MemoryClass memoryClass, MemoryMethod memoryMethod);

    public abstract String mapLocalVariable(String className, MemoryClass memoryClass, MemoryMethod memoryMethod, MemoryLocalVariable localVariable);

    /**
     * Generates mappings for the provided jar file
     *
     * @param memoryJar instance of the MemoryJar that you want to map
     */

    public void map(MemoryJar memoryJar) {
        // get all the classes loaded in the jar
        Map<String, MemoryClass> classes = memoryJar.getClasses();

        // map all the classes that are not inner classes
        classes.forEach((className, memoryClass) -> {
            // check that the class is not an inner class
            if (!memoryClass.isInnerClass) {
                // generate mappings for the current class
                generateClass(className, memoryClass);
            }
        });

        // map all the inner classes
        classes.forEach((className, memoryClass) -> {
            // check that the class is an inner class
            if (memoryClass.isInnerClass) {
                // generate mappings for the current class
                generateClass(className, memoryClass);
            }
        });

        // map all method overrides
        classes.forEach((className, memoryClass) -> {
            // get the mapping for the current class
            Mapping classMapping = mappingManager.getClass(className);

            // map all the current class overrides
            mapOverrides(memoryClass, classMapping != null ? classMapping.mapping : className);
        });

        // map all the super fields
        classes.forEach((className, memoryClass) -> {
            // get the mapping for the current class
            Mapping classMapping = mappingManager.getClass(className);

            // select the current class
            mappingManager.mapClass(className, classMapping != null ? classMapping.mapping : className);

            // loop through all the super fields
            memoryClass.superFields.forEach((superClass, fields) -> fields.forEach(memoryField -> {
                // get the field mapping
                Mapping mapping = mappingManager.getField(superClass.name(), memoryField.name(), memoryField.description());

                // if the field is found
                if (mapping != null) {

                    // map the super field for the current class
                    mappingManager.mapField(mapping.name, mapping.mapping, memoryField.description());
                }
            }));
        });
    }

    /**
     * Generates mappings for the provided class
     *
     * @param className   name of the class that you want to map
     * @param memoryClass instance of the MemoryClass of the class that you want to map
     */

    void generateClass(String className, MemoryClass memoryClass) {
        // check that the current class is not a main class
        if (!memoryClass.isMainClass) {
            // create a string builder that will hold the mapping
            StringBuilder builder = new StringBuilder();

            // check if the class has an outer class if so generate an inner class mapping
            if (memoryClass.outerClass() != null) {

                // get the outer class name
                String outerClassName = memoryClass.outerClass().name();

                // get the mapping
                Mapping mapping = mappingManager.getClass(outerClassName);

                // get the outer name
                String outerName = mapping != null ? mapping.mapping : outerClassName;

                // build the mapping
                builder.append(outerName);
                builder.append("$");
                builder.append(mapClass(className, memoryClass));

                // add the inner class to the outer class inner class list
                memoryClass.outerClass().addInnerClass(0, builder.toString());
            } else {
                // reset the builder with the correct mapping
                builder = new StringBuilder(mapClass(className, memoryClass));
            }

            // map the class with its new mapping
            mappingManager.mapClass(className, builder.toString());
        }

        // check that the current class is not an enum
        if (!memoryClass.isEnum()) {

            // loop through all the fields in the class and map them
            memoryClass.fields.forEach(memoryField -> mappingManager.mapField(
                    memoryField.name(),
                    mapField(className, memoryClass, memoryField),
                    memoryField.description()
            ));
        }

        // loop through all the methods in the class and map them
        memoryClass.methods.stream().filter(MemoryMethod::isChangeable).forEach(memoryMethod -> {

            // if the method is not found
            if (!memoryMethod.isOverride()) {

                // generate the mapping
                String mapping = mapMethod(className, memoryClass, memoryMethod);

                // map the current method
                String description = memoryMethod.description();

                // map the method
                mappingManager.mapMethod(
                        memoryMethod.name(),
                        mapping,
                        description.substring(description.indexOf(")") + 1),
                        description
                );
            }

            // map all the local variables
            memoryMethod.localVariables.forEach(localVariable
                    -> localVariable.setName(mapLocalVariable(className, memoryClass, memoryMethod, localVariable)));
        });
    }

    /**
     * Maps any override methods in the provided class
     *
     * @param memoryClass  class that you want to map the overrides for
     * @param classMapping mapping of the class that you want to map the overrides for
     */

    void mapOverrides(MemoryClass memoryClass, String classMapping) {
        // select the current class as the target for the mapping manager
        mappingManager.mapClass(memoryClass.name(), classMapping);

        // loop through all the override methods
        memoryClass.methods.stream().filter(MemoryMethod::isOverride).forEach(memoryMethod -> {
            // get the method that was overridden
            MethodMapping methodMapping = mappingManager.getMethod(
                    memoryMethod.baseClass.name(),
                    memoryMethod.baseMethod.name(),
                    memoryMethod.baseMethod.description()
            );

            // if the method was found
            if (methodMapping != null) {
                // map the current method to the mapping of the method that it overrode
                mappingManager.mapMethod(
                        memoryMethod.baseMethod.name(),
                        methodMapping.mapping,
                        methodMapping.returnType,
                        methodMapping.description
                );
            }
        });
    }

}
