package me.mat.jprocessor.jar.memory;

import org.objectweb.asm.tree.AnnotationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class MemoryAnnotatedElement {

    public final List<MemoryAnnotation> annotations = new ArrayList<>();

    /**
     * Loads all the annotations
     *
     * @param annotations annotations that you want to load
     * @param classes     map of all the loaded classes
     */

    protected void init(List<AnnotationNode> annotations, Map<String, MemoryClass> classes) {
        // clear all the previous annotations
        this.annotations.clear();

        // if the list is valid
        if (annotations != null) {

            // loop through all the annotation nodes
            annotations.forEach(annotationNode -> {

                // get the class name of the annotation
                String annotationClass = annotationNode.desc.substring(1, annotationNode.desc.length() - 1);

                // if the classes pool contains the annotation class
                if (classes.containsKey(annotationClass)) {

                    // add the annotation to the annotations list
                    this.annotations.add(new MemoryAnnotation(annotationNode, classes.get(annotationClass)));
                }
            });
        }
    }

    /**
     * Checks if the current element has the provided annotation
     *
     * @param name name of the annotation that you want to check for
     * @return {@link Boolean}
     */

    public boolean isAnnotationPresent(String name) {
        return getAnnotation(name) != null;
    }

    /**
     * Checks if the current element has the provided annotation
     *
     * @param annotation class of the annotation that you want to check
     * @return {@link Boolean}
     */

    public boolean isAnnotationPresent(Class<?> annotation) {
        return isAnnotationPresent(annotation.getName().replaceAll("\\.", "/"));
    }

    /**
     * Gets the annotation from the current element
     *
     * @param name name of the annotation that you want to get
     * @return {@link MemoryAnnotation}
     */

    public MemoryAnnotation getAnnotation(String name) {
        return annotations.stream().filter(memoryAnnotation -> memoryAnnotation.annotationClass.name().equals(name)).findFirst().orElse(null);
    }

    /**
     * Gets the annotation from the current element
     *
     * @param annotation class of the annotation that you want to get
     * @return {@link MemoryAnnotation}
     */

    public MemoryAnnotation getAnnotation(Class<?> annotation) {
        return getAnnotation(annotation.getName().replaceAll("\\.", "/"));
    }

}
