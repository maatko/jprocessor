package me.mat.jprocessor.jar.cls;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@RequiredArgsConstructor
public class MemoryClass {

    public final List<MemoryField> fields = new ArrayList<>();

    public final List<MemoryMethod> methods = new ArrayList<>();

    @NonNull
    public ClassNode classNode;

    public MemoryClass superClass;

    /**
     * Initializes the class in the memory
     *
     * @param classes map of loaded classes for the loaded jar
     */

    public void initialize(Map<String, MemoryClass> classes) {
        // clear all the fields and methods
        fields.clear();
        methods.clear();

        // load all the fields into the memory
        classNode.fields.forEach(fieldNode -> fields.add(new MemoryField(fieldNode)));

        // load all the methods into the memory
        classNode.methods.forEach(methodNode -> methods.add(new MemoryMethod(methodNode)));

        // find the super class
        this.findSuperClass(classes);
    }

    /**
     * Finds all the methods that this class overrides from parenting classes
     *
     * @param superClass super that that will be searched
     */

    public void findOverrides(MemoryClass superClass) {
        if (superClass == null) {
            return;
        }

        methods.forEach(memoryMethod
                -> superClass.methods.stream().filter(memoryMethod::equals).findFirst().ifPresent(mm
                -> memoryMethod.originalMethod = mm));

        findOverrides(superClass.superClass);
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
