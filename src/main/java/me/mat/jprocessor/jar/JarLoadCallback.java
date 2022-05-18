package me.mat.jprocessor.jar;

import me.mat.jprocessor.jar.MemoryJar;

public interface JarLoadCallback {

    void onLoad(MemoryJar memoryJar);

    void onFail(String reason);

}
