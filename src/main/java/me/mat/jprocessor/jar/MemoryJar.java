package me.mat.jprocessor.jar;

import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.util.JarUtil;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MemoryJar {

    public final Map<String, MemoryClass> loadedClasses = new HashMap<>();

    public final Map<String, MemoryResource> loadedResources = new HashMap<>();

    public MemoryJar(File file) {
        // log to console that the jar's classes are loading into the memory
        JProcessor.Logging.info("Loading '%s' classes into memory", file.getName());

        // load all the classes into the memory
        JarUtil.load(file).forEach(classNode -> loadedClasses.put(classNode.name, new MemoryClass(classNode)));

        // setup the class hierarchy
        loadedClasses.forEach((className, memoryClass) -> memoryClass.initialize(loadedClasses));

        // log to console how many classes were loaded
        JProcessor.Logging.info("Loaded '%d' classes into memory", loadedClasses.size());

        // log to console that the jar's resources are loading into the memory
        JProcessor.Logging.info("Loading '%s' resources into memory", file.getName());

        // load all the resources
        JarUtil.loadResource(file, loadedResources);

        JProcessor.Logging.info("Loaded '%d' resources into memory", loadedResources.size());

        // log to console that the
        JProcessor.Logging.info("Setting up the class hierarchy");

        // build the class hierarchy
        loadedClasses.forEach((className, memoryClass) -> memoryClass.findOverrides(memoryClass.superClass));
    }

}
