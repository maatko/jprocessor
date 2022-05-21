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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MappingGenerator {

    private final Map<String, MemoryClass> parenting = new HashMap<>();

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

        // create a list that will contain all the methods that might be overridden in the current class
        List<MemoryMethod> overriddenMethods = new ArrayList<>();

        // collect all the methods from the super classes
        collectSuperMethods(memoryClass.superClass, overriddenMethods);

        // collect all the methods from the extended interfaces
        List<String> interfaces = memoryClass.classNode.interfaces;
        if (interfaces != null && !interfaces.isEmpty()) {
            interfaces.forEach(inf -> {
                MemoryClass interfaceClass = memoryJar.getClass(inf);
                if (interfaceClass != null) {
                    collectSuperMethods(interfaceClass, overriddenMethods);
                }
            });
        }

        // loop through all the methods in the class and map them
        memoryClass.methods.forEach(memoryMethod -> {

            // check if method is changeable
            if (!((memoryClass.isMainClass && memoryMethod.isMainMethod())
                    || !memoryMethod.isChangeable())) {

                // get the method that was overridden
                MemoryMethod overrideMethod = null;
                for (MemoryMethod method : overriddenMethods) {
                    if (method.equals(memoryMethod)) {
                        overrideMethod = method;
                    }
                }

                // if the method is not found
                if (overrideMethod == null) {
                    // generate the mapping
                    String mapping = mapMethod(className, memoryClass, memoryMethod);

                    // map the current method
                    String description = memoryMethod.methodNode.desc;
                    mappingManager.mapMethod(
                            memoryMethod.methodNode.name,
                            mapping,
                            description.substring(description.indexOf(")") + 1),
                            description
                    );

                    // if this is an annotation method
                    if (isAnnotation) {

                        // map the annotations for the current method
                        mapAnnotations(memoryJar, memoryClass, memoryMethod, mapping);
                    }
                } else {

                    // update the parenting hierarchy
                    parenting.put(overrideMethod.methodNode.name + overrideMethod.methodNode.desc, overrideMethod.parent);
                }
            }
        });

        // loop through all the methods
        memoryClass.methods.forEach(memoryMethod -> {

            // get the description of the method
            String description = memoryMethod.methodNode.name + memoryMethod.methodNode.desc;

            // check if the method is an override
            if (parenting.containsKey(description)) {

                // get the method that was overridden
                MethodMapping methodMapping = mappingManager.getMethod(
                        parenting.get(description).classNode.name,
                        memoryMethod.methodNode.name,
                        memoryMethod.methodNode.desc
                );

                // if the method was found
                if (methodMapping != null) {

                    // map the current method to the mapping of the method that it overrode
                    mappingManager.mapMethod(
                            memoryMethod.methodNode.name,
                            mapMethod(className, memoryClass, memoryMethod),
                            methodMapping.returnType,
                            methodMapping.description
                    );
                }
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
                mapAnnotation(memoryClass.classNode.visibleAnnotations, memoryMethod, mapping);
                mapAnnotation(memoryClass.classNode.invisibleAnnotations, memoryMethod, mapping);

                memoryClass.fields.forEach(memoryField -> {
                    mapAnnotation(memoryField.fieldNode.visibleAnnotations, memoryMethod, mapping);
                    mapAnnotation(memoryField.fieldNode.invisibleAnnotations, memoryMethod, mapping);
                });
                memoryClass.methods.forEach(method -> {
                    mapAnnotation(method.methodNode.visibleAnnotations, memoryMethod, mapping);
                    mapAnnotation(method.methodNode.invisibleAnnotations, memoryMethod, mapping);
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
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i).equals(memoryMethod.methodNode.name)) {
                        targetIndex = i;
                    }
                }
                visibleAnnotation.values.set(targetIndex, mapping);
            }
        }
    }

    /**
     * Collects all the methods from the super classes
     * into a provided list
     *
     * @param memoryClass super class that you want to collect from first
     * @param methods     a list of methods that the methods will be placed into
     */

    void collectSuperMethods(MemoryClass memoryClass, List<MemoryMethod> methods) {
        if (memoryClass == null) {
            return;
        }
        memoryClass.methods.stream().filter(MemoryMethod::isChangeable).forEach(methods::add);
        collectSuperMethods(memoryClass.superClass, methods);
    }

}
