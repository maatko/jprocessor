package me.mat.jprocessor.jar;

import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.util.JarUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;

public class MemoryJar {

    public final Map<String, MemoryClass> classes = new HashMap<>();

    public final Map<String, MemoryResource> resources = new HashMap<>();

    public MemoryJar(File file) {
        // log to console that the jar's classes are loading into the memory
        JProcessor.Logging.info("Loading '%s' classes into memory", file.getName());

        // load all the classes into the memory
        JarUtil.load(file).forEach(classNode -> classes.put(classNode.name, new MemoryClass(classNode)));

        // setup the class hierarchy
        classes.forEach((className, memoryClass) -> memoryClass.initialize(classes));

        // log to console how many classes were loaded
        JProcessor.Logging.info("Loaded '%d' classes into memory", classes.size());

        // log to console that the jar's resources are loading into the memory
        JProcessor.Logging.info("Loading '%s' resources into memory", file.getName());

        // load all the resources
        JarUtil.loadResource(file, resources);

        // log to console how many resources were loaded into memory
        JProcessor.Logging.info("Loaded '%d' resources into memory", resources.size());

        // log to console that the
        JProcessor.Logging.info("Setting up the class hierarchy");

        // build the class hierarchy
        classes.forEach((className, memoryClass) -> memoryClass.findOverrides(memoryClass.superClass));
    }

    /**
     * Saves the jar from memory
     * to a file on the disk
     *
     * @param file file that you want to save to
     */

    public void save(File file) {
        // log to console that the jar from memory is being saved to a file
        JProcessor.Logging.info("Saving the jar from memory to '%s'", file.getAbsolutePath());

        // create the jar output stream
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(file.toPath()))) {

            // alert the user that classes are being written
            JProcessor.Logging.info("Writing %d classes...", classes.size());

            // loop through all the class nodes
            classes.forEach((name, memoryClass) -> memoryClass.write(out));

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

}
