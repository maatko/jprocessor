package me.mat.jprocessor.jar.cls;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MemoryClass {

    public final List<MemoryMethod> methods = new ArrayList<>();

    @NonNull
    public ClassNode classNode;

    public MemoryClass superClass;

    /**
     * Initializes the class in the memory
     *
     * @param loadedClasses map of loaded classes for the loaded jar
     */

    public void initialize(Map<String, MemoryClass> loadedClasses) {
        // load all the methods into the memory
        classNode.methods.forEach(methodNode -> methods.add(new MemoryMethod(methodNode)));

        // find the super class
        this.findSuperClass(loadedClasses);
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
     * Finds the super class from the loaded classes
     *
     * @param loadedClasses map of loaded classes for the loaded jar
     */

    void findSuperClass(Map<String, MemoryClass> loadedClasses) {
        String superName = classNode.superName;
        if (superName != null && loadedClasses.containsKey(superName)) {
            superClass = loadedClasses.get(superName);
        }
    }

}
