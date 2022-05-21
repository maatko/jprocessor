package me.mat.jprocessor.jar.cls;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@RequiredArgsConstructor
public class MemoryClass {

    public final Map<String, MemoryInnerClass> innerClasses = new HashMap<>();

    public final List<MemoryField> fields = new ArrayList<>();

    public final List<MemoryMethod> methods = new ArrayList<>();

    @NonNull
    public ClassNode classNode;

    public MemoryClass outerClass;
    public MemoryClass superClass;

    public boolean isMainClass;
    public boolean isInnerClass;

    /**
     * Initializes the class in the memory
     *
     * @param classes map of loaded classes for the loaded jar
     */

    public void initialize(Map<String, MemoryClass> classes) {
        // clear all the fields and methods
        fields.clear();
        methods.clear();

        // get the outer class
        outerClass = classes.get(classNode.outerClass);

        // load all the fields into the memory
        classNode.fields.forEach(fieldNode -> fields.add(new MemoryField(fieldNode)));

        // load all the methods into the memory
        classNode.methods.forEach(methodNode -> methods.add(new MemoryMethod(methodNode)));

        // load all the inner classes into the memory
        classNode.innerClasses.forEach(innerClassNode -> {
            MemoryInnerClass innerClass = new MemoryInnerClass(innerClassNode);
            if (classes.containsKey(innerClass.classNode.name)) {
                innerClass.outerClass = classes.get(innerClass.classNode.name);
                innerClass.outerClass.isInnerClass = true;
            }
            innerClasses.put(innerClassNode.name, innerClass);
        });

        // find the super class
        this.findSuperClass(classes);
    }

    /**
     * Adds an inner class to the current class in memory
     *
     * @param access    access of the inner class
     * @param name      name of the inner class
     * @param outerName outer name of the inner class
     * @param innerName inner name of the inner class
     * @return {@link MemoryInnerClass}
     */

    public MemoryInnerClass addInnerClass(int access, String name, String outerName, String innerName) {
        InnerClassNode innerClassNode = new InnerClassNode(name, outerName, innerName, access);
        classNode.innerClasses.add(innerClassNode);

        MemoryInnerClass memoryInnerClass = new MemoryInnerClass(innerClassNode);
        innerClasses.put(name, memoryInnerClass);
        return memoryInnerClass;
    }

    /**
     * Adds an inner class to the current class in memory
     *
     * @param access access of the inner class
     * @param name   name of the inner class
     * @return {@link MemoryInnerClass}
     */

    public MemoryInnerClass addInnerClass(int access, String name) {
        return addInnerClass(access, name, null, null);
    }

    /**
     * Adds a field to the current class in memory
     *
     * @param access     access of the field
     * @param name       name of the field
     * @param descriptor descriptor of the field
     * @param signature  signature of the field
     * @param value      value of the field
     * @return {@link MemoryField}
     */

    public MemoryField addField(int access, String name, String descriptor, String signature, Object value) {
        return addField(Opcodes.ASM9, access, name, descriptor, signature, value);
    }

    /**
     * Adds a field to the current class in memory
     *
     * @param api        version of the ASM api that you want to use
     * @param access     access of the field
     * @param name       name of the field
     * @param descriptor descriptor of the field
     * @param signature  signature of the field
     * @param value      value of the field
     * @return {@link MemoryField}
     */

    public MemoryField addField(int api, int access, String name, String descriptor, String signature, Object value) {
        FieldNode fieldNode = new FieldNode(api, access, name, descriptor, signature, value);
        classNode.fields.add(fieldNode);

        MemoryField memoryField;
        fields.add(memoryField = new MemoryField(fieldNode));
        return memoryField;
    }

    /**
     * Adds a method to the current class in memory
     *
     * @param access     access of the method
     * @param name       name of the method
     * @param descriptor descriptor of the method
     * @param signature  signature of the method
     * @param exceptions array of exceptions that this method might throw
     * @return {@link MemoryMethod}
     */

    public MemoryMethod addMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return addMethod(Opcodes.ASM9, access, name, descriptor, signature, exceptions);
    }

    /**
     * Adds a method to the current class in memory
     *
     * @param api        version of the ASM api that you want to use
     * @param access     access of the method
     * @param name       name of the method
     * @param descriptor descriptor of the method
     * @param signature  signature of the method
     * @param exceptions array of exceptions that this method might throw
     * @return {@link MemoryMethod}
     */

    public MemoryMethod addMethod(int api, int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodNode methodNode = new MethodNode(api, access, name, descriptor, signature, exceptions);
        classNode.methods.add(methodNode);

        MemoryMethod memoryMethod;
        methods.add(memoryMethod = new MemoryMethod(methodNode));
        return memoryMethod;
    }

    public MemoryMethod getMethod(MemoryMethod memoryMethod) {
        return getMethod(memoryMethod.methodNode.name, memoryMethod.methodNode.desc);
    }

    public MemoryMethod getMethod(String name, String description) {
        return methods.stream().filter(memoryMethod -> memoryMethod.methodNode.name.equals(name) && memoryMethod.methodNode.desc.equals(description)).findFirst().orElse(null);
    }

    /**
     * Writes the class to the provided JarOutputStream
     *
     * @param outputStream stream that you want to write the class to
     */

    public void write(JarOutputStream outputStream) {
        try {
            // create the class writer
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            // load the class bytes into the class writer
            classNode.accept(classWriter);

            // load a new entry into the jar
            outputStream.putNextEntry(new JarEntry(classNode.name + ".class"));

            // write to that jar entry
            outputStream.write(classWriter.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the current class is
     * a broken inner class
     *
     * @return {@link Boolean}
     */

    public boolean isBrokenInnerClass() {
        return classNode.name.contains("$") && classNode.outerClass == null;
    }

    /**
     * Checks if the class is an enum
     *
     * @return {@link Boolean}
     */

    public boolean isEnum() {
        return classNode.superName != null && classNode.superName.toLowerCase().contains("enum");
    }

    /**
     * Finds the super class from the loaded classes
     *
     * @param classes map of loaded classes for the loaded jar
     */

    void findSuperClass(Map<String, MemoryClass> classes) {
        String superName = classNode.superName;
        if (superName != null && classes.containsKey(superName)) {
            superClass = classes.get(superName);
        }
    }

}
