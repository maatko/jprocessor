package me.mat.jprocessor.jar.clazz;

import lombok.Getter;
import me.mat.jprocessor.util.JarUtil;

import java.io.File;
import java.io.IOException;
import java.util.jar.Manifest;

public class MemoryManifest {

    @Getter
    private Manifest manifest;

    public MemoryManifest(File file) {
        try {
            this.manifest = JarUtil.getManifest(file);
        } catch (IOException e) {
            this.manifest = new Manifest();
        }
        this.manifest.getEntries().clear();
    }

}
