package me.mat.jprocessor;

import me.mat.jprocessor.jar.MemoryJar;

public interface JarLoadCallback {

    void onLoad(MemoryJar memoryJar);

    void onFail(String reason);

}
