package me.mat.jprocessor.jar.memory;

import lombok.Getter;
import me.mat.jprocessor.util.JarUtil;

import java.io.File;
import java.util.jar.Manifest;

public class MemoryManifest {

    @Getter
    private final Manifest manifest;

    public String mainClass;

    public MemoryManifest(File file) {
        // get the manifest from the jar
        this.manifest = JarUtil.getManifest(file);

        // if the manifest was not found
        if (this.manifest == null) {

            // return out of the method
            return;
        }

        // clear all the signatures
        this.manifest.getEntries().clear();

        // get the main class
        this.mainClass = manifest.getMainAttributes().getValue("Main-Class");

        // if the main class was found
        if (this.mainClass != null) {

            // fix the path of the main class
            this.mainClass = this.mainClass.replaceAll("\\.", "/");
        }
    }

}
