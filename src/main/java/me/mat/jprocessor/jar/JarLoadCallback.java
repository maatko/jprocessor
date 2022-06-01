package me.mat.jprocessor.jar;

import me.mat.jprocessor.jar.memory.MemoryJar;

public interface JarLoadCallback {

    void onLoad(MemoryJar memoryJar);

    void onFail(String reason);

}
