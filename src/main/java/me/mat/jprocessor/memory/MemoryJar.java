package me.mat.jprocessor.memory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public class MemoryJar {

    private final Map<String, MemoryClass> classPath = new HashMap<>();
    private final Map<String, MemoryClass> classMap = new HashMap<>();
    private final Map<String, MemoryResource> resourceMap = new HashMap<>();

    private final List<String> missingClasses = new ArrayList<>();

    public final MemoryManifest manifest;

    public MemoryJar(File file, File... libraries) {`
        // load all the data from the file
        this.manifest = new MemoryManifest(loadJar(file, classMap, resourceMap));

        // load the base class to the class path
        classPath.putAll(classMap);

        // loop through all the libraries and load them into the class path
        Stream.of(libraries).forEach(library -> loadJar(library, classPath, null));

        // loop through all the classes and build them
        classMap.values().forEach(memoryClass -> memoryClass.build(this));

        // loop through all the classes and print the to the console
        classMap.forEach((s, memoryClass) -> System.out.println(memoryClass));
    }

    /**
     * Gets the {@link MemoryClass} from the class path
     *
     * @param className name of the class that you want to fetch
     *
     * @return {@link MemoryClass} that was found
     */

    public MemoryClass getFromClassPath(String className) {
        // check that the super name is valid
        if (className == null) {
            return null;
        }

        // make sure that the class pool contains the super class
        if (!classPath.containsKey(className)) {
            // get the class from the system
            try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(className + ".class")) {
                // if the class was not found throw an exception
                if (inputStream == null)
                    throw new IOException("Could not find the class: " + className);

                // create the class from the input stream data
                MemoryClass memoryClass = new MemoryClass(readStream(inputStream));

                // cache the class into the class path
                classPath.put(className, memoryClass);

                // return the class
                return memoryClass;
            } catch (IOException e) {
                // add the class name to the missing class list
                missingClasses.add(className);

                // and return null
                return null;
            }
        }

        // get the super class from the class pool
        return classPath.get(className);
    }

    /**
     * Loads all the classes & resources from the {@link File}
     * into the provided maps
     *
     * @param file        {@link File} that you want to load
     * @param classMap    {@link Map} where you want to store all the classes
     * @param resourceMap {@link Map} where you want to store all the resources
     */

    private static Manifest loadJar(File file, Map<String, MemoryClass> classMap, Map<String, MemoryResource> resourceMap) {
        // open the jar file
        try (JarFile jarFile = new JarFile(file)) {
            // loop through all the entries of the jar file
            jarFile.stream().forEach(jarEntry -> {
                try {
                    // read the data of the entry
                    byte[] data = readStream(jarFile.getInputStream(jarEntry));

                    // get the name of the entry
                    String path = jarEntry.getName();

                    // if the entry is a class
                    if (path.endsWith(".class")) {
                        // remove the class suffix
                        path = path.substring(0, path.lastIndexOf("."));

                        // and add the class to the class map
                        classMap.put(path, new MemoryClass(data));
                    } else if (!jarEntry.isDirectory()) {
                        // if the resource map was provided
                        if (resourceMap != null) {
                            // add the resource to the cache
                            resourceMap.put(path, new MemoryResource(data));
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            // return the jars manifest
            return jarFile.getManifest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads data from an {@link InputStream} into an array of {@link Byte}
     *
     * @param inputStream {@link InputStream} that you want to read from
     *
     * @return array of {@link Byte} that was read from the {@link InputStream}
     */

    private static byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[0x1000];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

}
