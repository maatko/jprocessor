package me.mat.jprocessor.memory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.jar.Manifest;

@Getter
@RequiredArgsConstructor
public class MemoryManifest {

    private final Manifest manifest;

    /**
     * Gets the name of the main class
     *
     * @return {@link String} name of the main class
     */

    public String getMainClass() {
        return manifest.getMainAttributes().getValue("Main-Class");
    }

    /**
     * Sets the main class of the manifest
     *
     * @param className {@link String} name of the class
     */

    public void setMainClass(String className) {
        put("Main-Class", className.replaceAll("/", "."));
    }

    /**
     * Puts a value into the manifest
     *
     * @param name  {@link String} name of the value
     * @param value {@link String} value
     */

    public void put(String name, String value) {
        manifest.getMainAttributes().putValue(name, value);
    }

}
