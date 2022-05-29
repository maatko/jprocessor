package me.mat.jprocessor.jar;

import lombok.Getter;
import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.jar.cls.MemoryManifest;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.transformer.ClassTransformer;
import me.mat.jprocessor.transformer.FieldTransformer;
import me.mat.jprocessor.transformer.MethodTransformer;
import me.mat.jprocessor.util.JarUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;

@Getter
public class MemoryJar {

    private final Map<String, MemoryClass> classes = new HashMap<>();

    private final Map<String, MemoryResource> resources = new HashMap<>();

    private MemoryManifest manifest;

    public MemoryJar(Map<String, byte[]> classData, String mainClass) {
        // log to console that the jar's classes are loading into the memory
        JProcessor.Logging.info("Loading from provided memory");

        // loop through all the class data
        classData.forEach((className, bytes) -> {

            // get the class node from the class data
            ClassNode classNode = JarUtil.getClassNode(bytes);

            // if the class node was created
            if (classNode != null) {

                // load the class node into memory jar
                classes.put(
                        className.replaceAll("\\.", "/"),
                        new MemoryClass(classNode)
                );
            } else {

                // else if it was not created log to console that it was an invalid class
                System.err.println("[!] Invalid class '" + className + "'");
            }
        });

        // attempt to fix broken inner classes
        classes.forEach((className, memoryClass) -> {

            // check if the memory class is a broken inner class
            if (memoryClass.isBrokenInnerClass()) {

                // if so get the outer class from the pool
                MemoryClass outerClass = classes.get(className.split("\\$")[0]);

                // if the class was found
                if (outerClass != null) {

                    // set classes outer class to the new outer class
                    memoryClass.setOuterClass(outerClass);
                }
            }
        });

        // setup the class hierarchy
        classes.forEach((className, memoryClass) -> memoryClass.initialize(classes));
        classes.forEach((className, memoryClass) -> memoryClass.buildHierarchy());

        // log to console how many classes were loaded
        JProcessor.Logging.info("Loaded '%d' classes into memory", classes.size());

        // if the classes pool contains the main class
        if (classes.containsKey(mainClass)) {

            // update the main class flag in the target class
            classes.get(mainClass).isMainClass = true;
        }
    }

    public MemoryJar(File file) {
        // log to console that the jar's classes are loading into the memory
        JProcessor.Logging.info("Loading '%s' into memory", file.getName());

        // load all the classes into the memory
        JarUtil.load(file).forEach(classNode -> classes.put(classNode.name, new MemoryClass(classNode)));

        // load the manifest from the jar file
        this.manifest = new MemoryManifest(file);

        // attempt to fix broken inner classes
        classes.forEach((className, memoryClass) -> {

            // check if the memory class is a broken inner class
            if (memoryClass.isBrokenInnerClass()) {

                // if so get the outer class from the pool
                MemoryClass outerClass = classes.get(className.split("\\$")[0]);

                // if the class was found
                if (outerClass != null) {

                    // set classes outer class to the new outer class
                    memoryClass.setOuterClass(outerClass);
                }
            }
        });

        // setup the class hierarchy
        classes.forEach((className, memoryClass) -> memoryClass.initialize(classes));
        classes.forEach((className, memoryClass) -> memoryClass.buildHierarchy());

        // log to console how many classes were loaded
        JProcessor.Logging.info("Loaded '%d' classes into memory", classes.size());

        // load all the resources
        JarUtil.loadResource(file, resources);

        // log to console how many resources were loaded into memory
        JProcessor.Logging.info("Loaded '%d' resources into memory", resources.size());

        // get the main class of the jar
        String mainClass = JarUtil.getMainClass(file).replaceAll("\\.", "/");
        if (classes.containsKey(mainClass)) {
            classes.get(mainClass).isMainClass = true;
        }
    }

    /**
     * Transforms all the classes
     * wit the provided class transformer
     *
     * @param classTransformer class transformer that you want to use to transform
     */

    public void transformClasses(ClassTransformer classTransformer) {
        classes.forEach((className, memoryClass) -> memoryClass.transform(classTransformer));
    }

    /**
     * Transforms all the fields
     * wit the provided field transformer
     *
     * @param fieldTransformer field transformer that you want to use to transform
     */

    public void transformFields(FieldTransformer fieldTransformer) {
        classes.forEach((className, memoryClass) -> memoryClass.transform(fieldTransformer));
    }

    /**
     * Transforms all the methods
     * wit the provided method transformer
     *
     * @param methodTransformer method transformer that you want to use to transform
     */

    public void transformMethods(MethodTransformer methodTransformer) {
        classes.forEach((className, memoryClass) -> memoryClass.transform(methodTransformer));
    }

    /**
     * Remaps the classes in memory based
     * on the mappings from the mapping manager
     *
     * @param mappingManager mappings that you want to use to remap
     */

    public void remap(MappingManager mappingManager) {
        // remap all the classes
        classes.forEach((className, memoryClass) -> memoryClass.map(this, mappingManager));

        // setup the class hierarchy
        classes.forEach((className, memoryClass) -> memoryClass.initialize(classes));
        classes.forEach((className, memoryClass) -> memoryClass.buildHierarchy());
    }

    /**
     * Creates a class in the memory
     * and loads it into the jar
     *
     * @param version    version of java that this will target
     * @param access     access of the class
     * @param name       name of the class
     * @param sig        signature of the class
     * @param superName  name of the parenting class
     * @param interfaces array of interfaces that the class has
     * @return {@link MemoryClass}
     */

    public MemoryClass createClass(int version, int access, String name,
                                   String sig, String superName, String[] interfaces) {
        return createClass(Opcodes.ASM9, version, access, name, sig, superName, interfaces);
    }

    /**
     * Creates a class in the memory
     * and loads it into the jar
     *
     * @param api        version of the ASM api that you want to use
     * @param version    version of java that this will target
     * @param access     access of the class
     * @param name       name of the class
     * @param sig        signature of the class
     * @param superName  name of the parenting class
     * @param interfaces array of interfaces that the class has
     * @return {@link MemoryClass}
     */

    public MemoryClass createClass(int api, int version, int access,
                                   String name, String sig, String superName,
                                   String[] interfaces) {
        ClassNode classNode = new ClassNode(api);
        classNode.visit(version, access, name, sig, superName, interfaces);
        classNode.visitSource(name + ".java", null);
        classNode.visitEnd();

        MemoryClass memoryClass = new MemoryClass(classNode);
        classes.put(name, memoryClass);

        memoryClass.initialize(classes);
        return memoryClass;
    }

    /**
     * Gets a class by the provided class name
     *
     * @param className name of the class that you want to get
     * @return {@link MemoryClass}
     */

    public MemoryClass getClass(String className) {
        return classes.getOrDefault(className, null);
    }

    /**
     * Exports all the classes to a map
     *
     * @return {@link Map}
     */

    public Map<String, byte[]> exportClasses() {
        // create a new map that will hold all the data
        Map<String, byte[]> data = new HashMap<>();

        // loop through all the class nodes and them to the map
        classes.forEach((name, memoryClass) -> data.put(name, memoryClass.write(this)));

        // return the data
        return data;
    }

    /**
     * Saves the jar from memory
     * to a file on the disk
     *
     * @param file    file that you want to save to
     * @param comment comment on the output jar file
     */

    public void save(File file, String comment) {
        // log to console that the jar from memory is being saved to a file
        JProcessor.Logging.info("Saving the jar from memory to '%s'", file.getAbsolutePath());

        // create the jar output stream
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(file.toPath()), manifest.getManifest())) {

            // if the comment is provided
            if (comment != null) {
                // set the jar comment
                out.setComment(comment);
            }

            // alert the user that classes are being written
            JProcessor.Logging.info("Writing %d classes...", classes.size());

            // loop through all the class nodes and write them to the stream
            classes.forEach((name, memoryClass) -> memoryClass.write(this, out));

            // alert the user that classes are finished writing
            JProcessor.Logging.info("Finished writing classes");

            // alert the user that resources are being saved
            JProcessor.Logging.info("Saving %d resources...", resources.size());

            // loop through all the resources
            resources.forEach((name, resource) -> resource.write(out, name));

            // alert the user that classes are finished writing
            JProcessor.Logging.info("Finished saving resources");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the jar from memory
     * to a file on the disk
     *
     * @param file file that you want to save to
     */

    public void save(File file) {
        save(file, null);
    }

    /**
     * Checks if the jar is loaded
     * aka is there is any classes
     * in the classes pool
     *
     * @return {@link Boolean}
     */

    public boolean isLoaded() {
        return !classes.isEmpty();
    }

}
