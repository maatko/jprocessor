package me.mat.jprocessor.jar;

public interface JarLoadCallback {

    void onLoad(MemoryJar memoryJar);

    void onFail(String reason);

}
