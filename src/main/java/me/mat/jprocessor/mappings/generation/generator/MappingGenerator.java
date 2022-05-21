package me.mat.jprocessor.mappings.generation.generator;

import lombok.Setter;
import me.mat.jprocessor.jar.MemoryJar;
import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.jar.cls.MemoryField;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.mapping.Mapping;

import java.util.Map;

public abstract class MappingGenerator {

    @Setter
    protected MappingManager mappingManager;

    public abstract String mapClass(String className, MemoryClass memoryClass);

    public abstract String mapField(String className, MemoryClass memoryClass, MemoryField memoryField);

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
            if (memoryClass.outerClass != null) {
                // get the mapping
                Mapping mapping = mappingManager.getClass(memoryClass.classNode.outerClass);

                // get the outer name
                String outerName = mapping != null ? mapping.mapping : memoryClass.classNode.outerClass;

                // update the outer name of the class node
                memoryClass.classNode.outerClass = outerName;

                // build the mapping
                builder.append(outerName);
                builder.append("$");
                builder.append(mapClass(className, memoryClass));

                // add the inner class to the outer class inner class list
                memoryClass.outerClass.addInnerClass(0, builder.toString());
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
                    memoryField.fieldNode.name,
                    mapField(className, memoryClass, memoryField),
                    memoryField.fieldNode.desc
            ));
        }
    }

}
