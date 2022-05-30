package me.mat.jprocessor.jar.clazz;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.InnerClassNode;

@RequiredArgsConstructor
public class MemoryInnerClass {

    @NonNull
    private InnerClassNode classNode;

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

    /**
     * Returns the name of the inner class
     *
     * @return {@link String}
     */

    public String name() {
        return classNode.name;
    }

}
