package me.mat.jprocessor.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.mat.jprocessor.jar.memory.MemoryClass;
import me.mat.jprocessor.jar.memory.MemoryResource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JarUtil {

    private static final String CLASS_SUFFIX = ".class";

    /**
     * Returns the Manifest of the provided jar file
     *
     * @param file file that you want to get the manifest for
     * @return {@link Manifest}
     */

    public static Manifest getManifest(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            return jarFile.getManifest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Loads a class into memory
     *
     * @param aClass class that you want to load
     * @return {@link MemoryClass}
     */

    public static MemoryClass load(Class<?> aClass) {
        // get the class input stream from the resources
        InputStream inputStream = ResourceUtil.getClassResource(aClass);
        if (inputStream == null) {
            throw new RuntimeException("Invalid Class Resource: " + aClass.getName());
        }

        // read the data from the class
        byte[] data;
        try {
            data = read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // load the class node
        ClassNode classNode = getClassNode(data);
        if (classNode == null) {
            throw new RuntimeException("Failed to load the ClassNode: " + aClass.getName());
        }

        // define a new memory class
        MemoryClass memoryClass = new MemoryClass(classNode);

        // initialize the class
        memoryClass.initialize(new HashMap<>());

        // build the class hierarchy
        memoryClass.buildHierarchy();

        // return the class
        return memoryClass;
    }

    /**
     * Loads all the resources from the
     * provided file into the provided map
     *
     * @param file      file that you want to load from
     * @param resources cache that you want to put the resources in
     */

    public static void loadResource(File file, Map<String, MemoryResource> resources) {
        // load the jar file
        try (JarFile jarFile = new JarFile(file)) {

            // get the enumeration for all the jar entries
            Enumeration<JarEntry> entries = jarFile.entries();

            // loop while there is valid jar entries
            while (entries.hasMoreElements()) {

                // get the current jar entry
                JarEntry jarEntry = entries.nextElement();

                // get current entries name
                String name = jarEntry.getName();

                // test it against the predicate
                if (!name.endsWith(CLASS_SUFFIX)
                        && !jarEntry.isDirectory()
                        && !name.endsWith("META-INF/MANIFEST.MF")
                        && !name.endsWith(".SF")
                        && !name.endsWith(".RSA")) {

                    // get the input stream from the jar for the current entry
                    InputStream inputStream = jarFile.getInputStream(jarEntry);

                    // if the stream is valid
                    if (inputStream != null) {

                        // read and put resources bytes into the resources pool
                        resources.put(name, new MemoryResource(read(inputStream)));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads all the jar classes
     *
     * @param jar jar that you want to load
     * @return {@link Stream<ClassNode>}
     */

    public static Stream<ClassNode> load(File jar) {
        return load(jar, name -> name.endsWith(CLASS_SUFFIX)).map(JarUtil::getClassNode);
    }

    /**
     * Load's the jar into memory and
     * returns a stream of loaded classes
     *
     * @param jar       that you want to load
     * @param predicate that you want to match for the class name
     * @return {@link Stream<byte> }
     */

    public static Stream<byte[]> load(File jar, Predicate<? super String> predicate) {
        // define a list that will hold all the class data
        List<byte[]> list = new ArrayList<>();

        // load the jar file
        try (JarFile jarFile = new JarFile(jar)) {

            // get the enumeration for all the jar entries
            Enumeration<JarEntry> entries = jarFile.entries();

            // loop while there is valid jar entries
            while (entries.hasMoreElements()) {

                // get the current jar entry
                JarEntry jarEntry = entries.nextElement();

                // test it against the predicate
                if (predicate.test(jarEntry.getName())) {

                    // get the input stream from the jar for the current entry
                    InputStream inputStream = jarFile.getInputStream(jarEntry);

                    // if the stream is valid
                    if (inputStream != null) {

                        // read the data from it and add it to the class data list
                        list.add(read(inputStream));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return the stream of class data
        return list.stream();
    }

    /**
     * Reads a class node from the provided data
     *
     * @param data data that you want to read into the class node
     * @return {@link ClassNode}
     */

    public static ClassNode getClassNode(byte[] data) {
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

        // else just return null
        return null;
    }

    /**
     * Reads a byte[] from an input stream
     *
     * @param in the input stream
     * @return byte[] of the input stream
     */

    public static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[0x1000];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

}