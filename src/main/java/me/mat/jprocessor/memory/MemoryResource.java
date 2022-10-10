package me.mat.jprocessor.memory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@RequiredArgsConstructor
public class MemoryResource {

    @NonNull
    private byte[] data;

    /**
     * Writes the resource data into a {@link JarOutputStream}
     *
     * @param path            path of the resource
     * @param jarOutputStream {@link JarOutputStream} that you want to write to
     */

    public void writeBytes(String path, JarOutputStream jarOutputStream) throws IOException {
        jarOutputStream.putNextEntry(new JarEntry(path));
        jarOutputStream.write(data);
    }

}
