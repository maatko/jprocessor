package me.mat.jprocessor.memory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
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

    public final Map<String, MemoryClass> interfaces = new HashMap<>();

    public final List<MemoryField> fields = new ArrayList<>();
    public final List<MemoryMethod> methods = new ArrayList<>();

    @NonNull
    private final ClassNode classNode;

    public MemoryClass superClass;
    public boolean isMainClass;

    public MemoryClass(byte[] data) {
        this(getClassNode(data));
    }

    /**
     * Writes the class data into a {@link JarOutputStream}
     *
     * @param className       name of the class that you want to write
     * @param jarOutputStream {@link JarOutputStream} that you want to write to
     */

    public void writeBytes(String className, JarOutputStream jarOutputStream) throws IOException {
        // put the new jar entry into the output stream
        jarOutputStream.putNextEntry(new JarEntry(className + ".class"));

        // write the class bytes to the stream
        jarOutputStream.write(getBytes());
    }

    /**
     * Gets the byte data from the class
     *
     * @return array of {@link Byte} containing the data of the class
     */

    public byte[] getBytes() {
        // create the class writer
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        // write the class node to the class writer
        classNode.accept(classWriter);

        // return the byte data from the class writer
        return classWriter.toByteArray();
    }

    /**
     * Builds the class from the {@link MemoryJar}
     *
     * @param memoryJar {@link MemoryJar} that you want to build the {@link MemoryClass} from
     */

    protected void build(MemoryJar memoryJar) {
        // get the super class from the class path
        superClass = memoryJar.getFromClassPath(classNode.superName);

        // if the super class was found
        if (superClass != null) {
            // build the super class
            superClass.build(memoryJar);
        }

        // get the list of interfaces
        List<String> interfaces = classNode.interfaces;

        // if the interfaces list contains classes
        if (interfaces != null && !interfaces.isEmpty()) {
            // loop through all the interfaces and load them into the interfaces list
            interfaces.forEach(className -> {
                // get the interface class
                MemoryClass interfaceClass = memoryJar.getFromClassPath(className);

                // if the interface class was found
                if (interfaceClass != null) {
                    // build the interface class
                    interfaceClass.build(memoryJar);

                    // cache it
                    this.interfaces.put(className, interfaceClass);
                }
            });
        }

        // get all the fields from the class
        List<FieldNode> fields = classNode.fields;

        // check that the fields list has any content
        if (fields != null && !fields.isEmpty()) {
            // load all the fields into the cache
            fields.forEach(fieldNode -> this.fields.add(new MemoryField(fieldNode)));
        }

        // get all the methods from the class
        List<MethodNode> methods = classNode.methods;

        // check that the methods list has any content
        if (methods != null && !methods.isEmpty()) {
            // load all the methods into the cache
            methods.forEach(methodNode -> this.methods.add(new MemoryMethod(methodNode)));

            // build all the method overrides
            this.buildOverrides();
        }
    }

    /**
     * Builds all the method overrides
     */

    private void buildOverrides() {
        // define a list that will hold all the super classes
        List<MemoryClass> superClasses = new ArrayList<>(interfaces.values());

        // if the super class is valid
        if (superClass != null) {
            // add it to the list
            superClasses.add(superClass);
        }

        // build all the overrides for the super classes
        superClasses.forEach(MemoryClass::buildOverrides);

        // loop through all the methods
        methods.forEach(memoryMethod -> superClasses.forEach(superClass -> getSuperMethod(superClass, memoryMethod)));
    }

    /**
     * Gets the super method from the super class
     *
     * @param superClass   {@link MemoryClass} super class that you want to search in
     * @param memoryMethod {@link MemoryMethod} method that you want to search for the override
     */

    private void getSuperMethod(MemoryClass superClass, MemoryMethod memoryMethod) {
        // loop through all the super class methods
        for (MemoryMethod method : superClass.methods) {
            // if the current method is equal to the provided method
            if (method.equals(memoryMethod)) {
                // update the provided methods superClass & superMethod
                memoryMethod.superClass = superClass;
                memoryMethod.superMethod = method;

                // break out of the loop
                break;
            }
        }
    }

    /**
     * Gets the name of the class
     *
     * @return {@link String} name of the class
     */

    public String getName() {
        return classNode.name;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(getName()).append(":").append("\n");

        builder.append("\tInterfaces:\n");
        interfaces.keySet().forEach(s -> builder.append("\t\t").append(s).append("\n"));

        builder.append("\tFields:\n");
        fields.forEach(memoryField -> builder.append("\t\t").append(memoryField.getName()).append("\n"));

        builder.append("\tMethods:\n");
        methods.forEach(memoryMethod -> builder.append("\t\t").append(memoryMethod.getName()).append(memoryMethod.superClass != null ? " @ " + memoryMethod.superClass.getName() : "").append("\n"));

        return builder.toString();
    }

    /**
     * Reads a class node from the provided data
     *
     * @param data data that you want to read into the class node
     *
     * @return {@link ClassNode} class node that was read from the data
     */

    private static ClassNode getClassNode(byte[] data) {
        // read the first 4 bytes of the array
        String cafeBabe = String.format("%02X%02X%02X%02X", data[0], data[1], data[2], data[3]);

        // check that the string that was read is equal to the cafe babe
        if (cafeBabe.equalsIgnoreCase("cafeBabe")) {

            // create the class reader from the input stream
            ClassReader classReader = new ClassReader(data);

            // create a new class node
            ClassNode classNode = new ClassNode();

            // write the bytes of the class to the class node
            classReader.accept(classNode, 0);

            // return the class node
            return classNode;
        }

        // throw a new runtime exception with the error information
        throw new RuntimeException("Invalid class data");
    }

}
