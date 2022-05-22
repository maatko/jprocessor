package me.mat.jprocessor.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.mat.jprocessor.jar.MemoryResource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JarUtil {

    private static final String CLASS_SUFFIX = ".class";

    /**
     * Returns the main class of the application
     *
     * @param file jar file that you want to retrive the main class for
     * @return {@link String}
     */

    public static String getMainClass(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            if (jarFile.getManifest().getMainAttributes().getValue("Main-Class") == null) {
                return "";
            }
            return jarFile.getManifest().getMainAttributes().getValue("Main-Class");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns the Manifest of the provided jar file
     *
     * @param file file that you want to get the manifest for
     * @return {@link Manifest}
     */

    public static Manifest getManifest(File file) throws IOException {
        try (JarFile jarFile = new JarFile(file)) {
            return jarFile.getManifest();
        } catch (Exception e) {
            throw new IOException(e);
        }
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
        return load(jar, name -> name.endsWith(CLASS_SUFFIX)).map(bytes -> {
            // read the first 4 bytes of the array
            String cafeBabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);

            // check that the string that was read is equal to the cafe babe
            if (cafeBabe.equalsIgnoreCase("cafeBabe")) {

                // create the class reader from the input stream
                ClassReader classReader = new ClassReader(bytes);

                // create a new class node
                ClassNode classNode = new ClassNode();

                // write the bytes of the class to the class node
                classReader.accept(classNode, 0);

                // return the class node
                return classNode;
            }

            // else return null
            return null;
        });
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
     * Reads a byte[] from an input stream
     *
     * @param in the input stream
     * @return byte[] of the input stream
     * @throws IOException
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