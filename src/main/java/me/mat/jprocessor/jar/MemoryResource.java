package me.mat.jprocessor.jar;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@RequiredArgsConstructor
public class MemoryResource {

    @NonNull
    private byte[] data;

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
