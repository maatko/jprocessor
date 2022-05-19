package me.mat.jprocessor.jar.cls;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.InnerClassNode;

@RequiredArgsConstructor
public class MemoryInnerClass {

    @NonNull
    public InnerClassNode classNode;

    public MemoryClass outerClass;

}
