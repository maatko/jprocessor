package me.mat.jprocessor.jar.cls;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.util.asm.ASMUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MemoryField {

    private final List<MemoryAnnotation> annotations = new ArrayList<>();

    @NonNull
    private FieldNode fieldNode;

    /**
     * Loads all the annotation for the current field
     *
     * @param classes map of all the loaded classes
     * @return {@link MemoryField}
     */

    public MemoryField init(Map<String, MemoryClass> classes) {
        // clear all the annotations
        this.annotations.clear();

        // get the list of annotations
        List<AnnotationNode> annotations = fieldNode.visibleAnnotations;

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

        // return the instance of the field
        return this;
    }

    /**
     * Checks if the current field has the provided annotation
     *
     * @param name name of the annotation that you want to check for
     * @return {@link Boolean}
     */

    public boolean isAnnotationPresent(String name) {
        return getAnnotation(name) != null;
    }

    /**
     * Gets the annotation from the current field
     *
     * @param name name of the annotation that you want to get
     * @return {@link MemoryAnnotation}
     */

    public MemoryAnnotation getAnnotation(String name) {
        return annotations.stream().filter(memoryAnnotation -> memoryAnnotation.annotationClass.name().equals(name)).findFirst().orElse(null);
    }

    /**
     * Gets the name of the field
     *
     * @return @{@link String}
     */

    public String name() {
        return fieldNode.name;
    }

    /**
     * Gets the description of the field
     *
     * @return @{@link String}
     */

    public String description() {
        return fieldNode.desc;
    }

    /**
     * Gets the visible annotations for the current field
     *
     * @return {@link List}
     */

    public List<AnnotationNode> getVisibleAnnotations() {
        return fieldNode.visibleAnnotations;
    }

    /**
     * Gets the invisible annotations for the current field
     *
     * @return {@link List}
     */

    public List<AnnotationNode> getInvisibleAnnotations() {
        return fieldNode.invisibleAnnotations;
    }

    /**
     * Checks if the field has final modifier
     *
     * @return {@link Boolean}
     */

    public boolean isFinal() {
        return Modifier.isFinal(fieldNode.access);
    }

    /**
     * Checks if the field has static modifier
     *
     * @return {@link Boolean}
     */

    public boolean iStatic() {
        return Modifier.isStatic(fieldNode.access);
    }

    /**
     * Gets the access of the field
     *
     * @return {@link Integer}
     */

    public int getAccess() {
        return fieldNode.access;
    }

    /**
     * Sets the access of the field
     *
     * @param access access that you want to set it to
     */

    public void setAccess(int access) {
        fieldNode.access = access;
    }

    /**
     * Checks if the instruction matches
     * the current field
     *
     * @param instruction instruction that you want to check aginst
     * @return {@link Boolean}
     */

    public boolean isCorrectInstruction(int instruction) {
        if (instruction == Opcodes.GETSTATIC && !iStatic()) {
            return false;
        } else if (instruction == Opcodes.PUTSTATIC && (!iStatic() || isFinal())) {
            return false;
        } else if (instruction == Opcodes.GETFIELD && iStatic()) {
            return false;
        } else return instruction != Opcodes.PUTFIELD || (!iStatic() && !isFinal());
    }

    /**
     * Checks if the provided object is equal to this object
     *
     * @param obj object that you are trying to check
     * @return {@link Boolean}
     */

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldNode) {
            return ASMUtil.isSameField((FieldNode) obj, fieldNode);
        } else if (!(obj instanceof MemoryField)) {
            return false;
        }
        return ASMUtil.isSameField(((MemoryField) obj).fieldNode, fieldNode);
    }

}
