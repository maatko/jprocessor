package me.mat.jprocessor.jar.memory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.remapper.JClassRemapper;
import me.mat.jprocessor.transformer.ClassTransformer;
import me.mat.jprocessor.transformer.FieldTransformer;
import me.mat.jprocessor.transformer.MethodTransformer;
import me.mat.jprocessor.util.asm.CustomClassWriter;
import me.mat.jprocessor.util.asm.IAccessed;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@RequiredArgsConstructor
public class MemoryClass extends MemoryAnnotatedElement implements IAccessed {

    public final Map<String, MemoryInnerClass> innerClasses = new HashMap<>();

    public final Map<MemoryClass, List<MemoryField>> superFields = new HashMap<>();

    public final Map<MemoryClass, List<MemoryMethod>> superMethods = new HashMap<>();

    public final Map<String, MemoryClass> interfaces = new HashMap<>();

    public final List<MemoryField> fields = new ArrayList<>();

    public final List<MemoryMethod> methods = new ArrayList<>();

    @NonNull
    private ClassNode classNode;

    private MemoryClass outerClass;

    private MemoryClass superClass;

    public boolean isMainClass;
    public boolean isInnerClass;

    /**
     * Initializes the class in the memory
     *
     * @param classes map of loaded classes for the loaded jar
     */

    public void initialize(Map<String, MemoryClass> classes) {
        // clear all the cache
        fields.clear();
        methods.clear();
        interfaces.clear();
        innerClasses.clear();
        superFields.clear();
        superMethods.clear();
        annotations.clear();

        // get the outer class
        outerClass = classes.get(classNode.outerClass);

        // load all the fields into the memory
        classNode.fields.forEach(fieldNode -> fields.add(new MemoryField(this, fieldNode).init(classes)));

        // load all the methods into the memory
        classNode.methods.forEach(methodNode -> methods.add(new MemoryMethod(this, methodNode).init(classes)));

        // find all the extended interfaces
        List<String> interfaces = classNode.interfaces;
        if (interfaces != null) {
            interfaces.forEach(className -> {
                if (classes.containsKey(className)) {
                    this.interfaces.put(className, classes.get(className));
                }
            });
        }

        // loop through all the inner classes
        classNode.innerClasses.forEach(innerClassNode -> {

            // create a memory inner class
            MemoryInnerClass innerClass = new MemoryInnerClass(innerClassNode);

            // get the name of the inner class
            String name = innerClass.name();

            // if the classes pool contains the inner class
            if (classes.containsKey(name)) {

                // set outer class of the inner class to the class from the pool that matches the name
                innerClass.outerClass = classes.get(name);

                // update the is inner class flag to true of the outer class
                innerClass.outerClass.isInnerClass = true;
            }

            // load the inner class to the map
            innerClasses.put(innerClassNode.name, innerClass);
        });

        // initialize all the annotations
        this.init(classNode.visibleAnnotations, classes);

        // find the super class
        this.findSuperClass(classes);
    }

    /**
     * Builds the class hierarchy aka
     * finds all the override methods
     */

    public void buildHierarchy() {
        // collect all the super classes that might have overrides
        List<MemoryClass> superClasses = new ArrayList<>();
        findSuperClasses(superClass, superClasses);
        interfaces.values().forEach(memoryClass -> findSuperClasses(memoryClass, superClasses));

        // loop through all the super classes and check for current class method overrides
        superClasses.forEach(memoryClass
                -> memoryClass.methods.forEach(override
                -> methods.forEach(method
                -> method.checkForOverride(memoryClass, override))));
    }

    /**
     * Transforms current class with the
     * provided class transformer
     *
     * @param classTransformer class transformer that you want to use
     */

    public void transform(ClassTransformer classTransformer) {
        // transform the class
        classTransformer.transform(this);

        // transform the fields and methods
        transform((FieldTransformer) classTransformer);
        transform((MethodTransformer) classTransformer);
    }

    /**
     * Transforms all the fields in the class
     * with the provided field transformer
     *
     * @param fieldTransformer field transformer that you want to use
     */

    public void transform(FieldTransformer fieldTransformer) {
        fields.forEach(memoryField -> fieldTransformer.transform(this, memoryField));
    }

    /**
     * Transforms all the methods in the class
     * with the provided method transformer
     *
     * @param methodTransformer method transformer that you want to use
     */

    public void transform(MethodTransformer methodTransformer) {
        // loop through all the methods
        methods.forEach(memoryMethod -> {
            // transform the method
            methodTransformer.transform(this, memoryMethod);

            // get the list of instructions
            MemoryInstructions instructions = memoryMethod.instructions;

            // loop through all the instructions
            for (int i = 0; i < instructions.size(); i++) {

                // and transform each instruction
                methodTransformer.transform(this, memoryMethod, instructions, instructions.get(i));
            }
        });
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
        fields.add(memoryField = new MemoryField(this, fieldNode));
        return memoryField;
    }

    /**
     * Attempts to find a field in one of the super classes
     * starting from the class
     *
     * @param name           name of the field
     * @param descriptor     descriptor of the field
     * @param instruction    instruction of the node to match with
     * @param classReference reference that the class will be stored in
     * @param fieldReference reference that the field will be stored in
     */

    public void findOverrideField(int instruction, String name, String descriptor, AtomicReference<MemoryClass> classReference, AtomicReference<MemoryField> fieldReference) {
        findOverrideField(this, name, descriptor, instruction, classReference, fieldReference);
    }

    /**
     * Attempts to find a field in one of the super classes
     * starting from the provided super class
     *
     * @param superClass     super class that you want to start the search with
     * @param name           name of the field
     * @param descriptor     descriptor of the field
     * @param instruction    instruction of the node to match with
     * @param classReference reference that the class will be stored in
     * @param fieldReference reference that the field will be stored in
     */

    void findOverrideField(MemoryClass superClass, String name,
                           String descriptor, int instruction,
                           AtomicReference<MemoryClass> classReference, AtomicReference<MemoryField> fieldReference) {
        // if the current super class is invalid
        if (superClass == null) {
            // return out of the method
            return;
        }

        // loop through all the fields
        for (MemoryField field : superClass.fields) {

            // if the instruction does not match the field
            if (!field.isCorrectInstruction(instruction)) {

                // continue in the loop
                continue;
            }

            // if the name and the descriptor match the field
            if (field.name().equals(name) && field.description().equals(descriptor)) {

                // update the references
                fieldReference.set(field);
                classReference.set(superClass);

                // return out of the method
                return;
            }
        }

        // search for the field in all the interfaces
        superClass.interfaces.forEach((className, interfaceClass)
                -> findOverrideField(interfaceClass, name, descriptor, instruction, classReference, fieldReference));

        // continue the search in the next super class
        findOverrideField(superClass.superClass, name, descriptor, instruction, classReference, fieldReference);
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
        methods.add(memoryMethod = new MemoryMethod(this, methodNode));
        return memoryMethod;
    }

    /**
     * Attempts to find a method in the super classes
     *
     * @param name            name of the method
     * @param descriptor      descriptor of the method
     * @param classReference  reference that the super class will be stored into
     * @param methodReference reference that the method will be stored into
     */

    public void findOverrideMethod(String name, String descriptor, AtomicReference<MemoryClass> classReference, AtomicReference<MemoryMethod> methodReference) {
        Map<MemoryClass, List<MemoryMethod>> methodMap = new HashMap<>();
        methodMap.put(this, methods);
        methodMap.putAll(superMethods);

        methodMap.forEach((superClass, methods)
                -> methods.stream().filter(memoryMethod
                -> memoryMethod.name().equals(name)
                && memoryMethod.description().equals(descriptor)).forEach(memoryMethod -> {
            classReference.set(superClass);
            methodReference.set(memoryMethod);
        }));
    }

    /**
     * Maps the current class based on the
     * mappings from the provided remapper
     *
     * @param mappingManager mappings that you want to use to map the class
     */

    public void map(MemoryJar memoryJar, MappingManager mappingManager) {
        ClassNode mappedNode = new ClassNode();
        JClassRemapper adapter = new JClassRemapper(mappedNode, memoryJar, mappingManager);

        classNode.accept(adapter);
        classNode = mappedNode;
    }

    /**
     * Writes the class to the provided JarOutputStream
     *
     * @param outputStream stream that you want to write the class to
     */

    public void write(JarOutputStream outputStream) {
        try {
            // create the class writer
            CustomClassWriter classWriter = new CustomClassWriter(ClassWriter.COMPUTE_MAXS);

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
     * Writes the class to a byte[]
     *
     * @return {@link Byte[]}
     */

    public byte[] write() {
        // create the class writer
        CustomClassWriter classWriter = new CustomClassWriter(ClassWriter.COMPUTE_MAXS);

        // load the class bytes into the class writer
        classNode.accept(classWriter);

        // return the data of the class writer
        return classWriter.toByteArray();
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
     * Returns the super class
     *
     * @return {@link MemoryClass}
     */

    public MemoryClass superClass() {
        return superClass;
    }

    /**
     * Returns the outer class
     *
     * @return {@link MemoryClass}
     */

    public MemoryClass outerClass() {
        return outerClass;
    }

    /**
     * Updates the current outer class
     *
     * @param memoryClass class that you want to set to
     */

    public void setOuterClass(MemoryClass memoryClass) {
        // if the memory class is valid
        if (memoryClass != null) {

            // update the outer class data
            this.outerClass = memoryClass;
            this.classNode.outerClass = memoryClass.name();
        }
    }

    /**
     * Returns the name of the class
     *
     * @return {@link String}
     */

    public String name() {
        return classNode.name;
    }

    /**
     * Gets the access of the class
     *
     * @return {@link Integer}
     */

    @Override
    public int getAccess() {
        return classNode.access;
    }

    /**
     * Sets the access of the class
     *
     * @param access access that you want to set it to
     */

    @Override
    public void setAccess(int access) {
        classNode.access = access;
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

    /**
     * Finds all the super classes for the current class
     * and loads them into a provided list
     *
     * @param memoryClass  super class that you want to begin the search with
     * @param superClasses list that the super classes will be populated in
     */

    void findSuperClasses(MemoryClass memoryClass, List<MemoryClass> superClasses) {
        if (memoryClass == null) {
            return;
        }

        // load all the super fields
        List<MemoryField> fields = superFields.getOrDefault(memoryClass, new ArrayList<>());
        fields.addAll(memoryClass.fields);
        superFields.put(memoryClass, fields);

        // load all the super methods
        List<MemoryMethod> methods = superMethods.getOrDefault(memoryClass, new ArrayList<>());
        methods.addAll(memoryClass.methods);
        superMethods.put(memoryClass, methods);

        // continue searching for other super classes
        superClasses.add(memoryClass);

        memoryClass.interfaces.forEach((className, interfaceClass) -> findSuperClasses(interfaceClass, superClasses));
        findSuperClasses(memoryClass.superClass, superClasses);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassNode) {
            return ((ClassNode) obj).name.equals(name());
        } else if (obj instanceof MemoryClass) {
            return ((MemoryClass) obj).name().equals(name());
        } else {
            return false;
        }
    }

}
