package me.mat.jprocessor.mappings.generation.generator;

import lombok.Setter;
import me.mat.jprocessor.jar.MemoryJar;
import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.jar.cls.MemoryField;
import me.mat.jprocessor.jar.cls.MemoryMethod;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.mapping.Mapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;
import java.util.Map;

public abstract class MappingGenerator {

    @Setter
    protected MappingManager mappingManager;

    public abstract String mapClass(String className, MemoryClass memoryClass);

    public abstract String mapField(String className, MemoryClass memoryClass, MemoryField memoryField);

    public abstract String mapMethod(String className, MemoryClass memoryClass, MemoryMethod memoryMethod);

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
                generateClass(className, memoryJar, memoryClass);
            }
        });

        // map all the inner classes
        classes.forEach((className, memoryClass) -> {
            // check that the class is an inner class
            if (memoryClass.isInnerClass) {
                // generate mappings for the current class
                generateClass(className, memoryJar, memoryClass);
            }
        });

        // map all method overrides
        classes.forEach((className, memoryClass) -> {
            // get the mapping for the current class
            Mapping classMapping = mappingManager.getClass(className);

            // map all the current class overrides
            mapOverrides(memoryClass, classMapping != null ? classMapping.mapping : className);
        });
    }

    /**
     * Generates mappings for the provided class
     *
     * @param className   name of the class that you want to map
     * @param memoryClass instance of the MemoryClass of the class that you want to map
     */

    void generateClass(String className, MemoryJar memoryJar, MemoryClass memoryClass) {
        // flag that checks if the class is an annotation
        boolean isAnnotation = memoryClass.isAnnotation();

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
                mappingManager.mapMethod(
                        memoryMethod.name(),
                        mapping,
                        description.substring(description.indexOf(")") + 1),
                        description
                );

                // if this is an annotation method
                if (isAnnotation) {

                    // map the annotations for the current method
                    mapAnnotations(memoryJar, memoryClass, memoryMethod, mapping);
                }
            }
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

    /**
     * Maps the annotation in all the loaded
     * classes in the jar
     *
     * @param memoryJar    handle of the current MemoryJar
     * @param parentClass  class of the annotation that it will be checked for
     * @param memoryMethod target method of the annotation
     * @param mapping      mapping that was generated for the provided method
     */

    void mapAnnotations(MemoryJar memoryJar, MemoryClass parentClass, MemoryMethod memoryMethod, String mapping) {
        memoryJar.getClasses().forEach((className, memoryClass) -> {
            if (!parentClass.equals(memoryClass)) {
                mapAnnotation(memoryClass.getVisibleAnnotations(), memoryMethod, mapping);
                mapAnnotation(memoryClass.getInvisibleAnnotations(), memoryMethod, mapping);

                memoryClass.fields.forEach(memoryField -> {
                    mapAnnotation(memoryField.getVisibleAnnotations(), memoryMethod, mapping);
                    mapAnnotation(memoryField.getInvisibleAnnotations(), memoryMethod, mapping);
                });
                memoryClass.methods.forEach(method -> {
                    mapAnnotation(method.getVisibleAnnotations(), memoryMethod, mapping);
                    mapAnnotation(method.getInvisibleAnnotations(), memoryMethod, mapping);
                });
            }
        });
    }

    /**
     * Maps annotations for the current method mapping
     *
     * @param annotations  list of annotations that you want to check
     * @param memoryMethod target method of the annotation
     * @param mapping      mapping that was generated for the provided method
     */

    void mapAnnotation(List<AnnotationNode> annotations, MemoryMethod memoryMethod, String mapping) {
        if (annotations != null) {
            for (AnnotationNode visibleAnnotation : annotations) {
                int targetIndex = -1;
                List<Object> values = visibleAnnotation.values;
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        if (values.get(i).equals(memoryMethod.name())) {
                            targetIndex = i;
                        }
                    }
                    if (targetIndex != -1) {
                        visibleAnnotation.values.set(targetIndex, mapping);
                    }
                }
            }
        }
    }

}
