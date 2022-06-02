package me.mat.jprocessor.jar.memory;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@RequiredArgsConstructor
public class MemoryResource {

    @Getter
    @NonNull
    private byte[] data;

    /**
     * Writes to contents of the resource
     * to the JarOutputStream with the provided name
     *
     * @param outputStream stream that you want to write to
     * @param name         name that you want to write the resource as
     */

    public void write(JarOutputStream outputStream, String name) {
        try {
            // load a new entry into the jar
            outputStream.putNextEntry(new JarEntry(name));

            // write to that jar entry
            outputStream.write(data, 0, data.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
