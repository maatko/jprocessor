package me.mat.jprocessor.jar.cls;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.InnerClassNode;

@RequiredArgsConstructor
public class MemoryInnerClass {

    @NonNull
    public InnerClassNode classNode;

    public MemoryClass outerClass;

    /**
     * Gets the access of the inner class
     *
     * @return {@link Integer}
     */

    public int getAccess() {
        return classNode.access;
    }

    /**
     * Sets the access of the inner class
     *
     * @param access access that you want to set it to
     */

    public void setAccess(int access) {
        classNode.access = access;
    }

}
