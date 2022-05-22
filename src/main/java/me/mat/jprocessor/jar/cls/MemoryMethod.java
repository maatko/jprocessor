package me.mat.jprocessor.jar.cls;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.util.asm.ASMUtil;
import org.objectweb.asm.tree.MethodNode;

@RequiredArgsConstructor
public class MemoryMethod {

    @NonNull
    public MemoryClass parent;

    @NonNull
    public MethodNode methodNode;

    public MemoryClass baseClass = null;
    public MemoryMethod baseMethod = null;

    /**
     * Checks if this method overrides the provided method
     *
     * @param baseClass  class the that method is from
     * @param baseMethod method that you want to check against
     */

    protected void checkForOverride(MemoryClass baseClass, MemoryMethod baseMethod) {
        if (equals(baseMethod)) {
            this.baseClass = baseClass;
            this.baseMethod = baseMethod;
        }
    }

    /**
     * Checks if the method can be remapped
     *
     * @return {@link Boolean}
     */

    public boolean isChangeable() {
        return ASMUtil.isChangeable(methodNode) && !isMainMethod();
    }

    /**
     * Checks if the method overrides a method
     * from one of the super classes
     *
     * @return {@link Boolean}
     */

    public boolean isOverride() {
        return baseClass != null && baseMethod != null;
    }

    /**
     * Checks if the method is a main method
     *
     * @return {@link Boolean}
     */

    public boolean isMainMethod() {
        return methodNode.name.equals("main") && methodNode.desc.equals("([Ljava/lang/String;)V");
    }

    /**
     * Checks if the provided object is equal to this object
     *
     * @param obj object that you are trying to check
     * @return {@link Boolean}
     */

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodNode) {
            return ASMUtil.isSameMethod((MethodNode) obj, methodNode);
        } else if (!(obj instanceof MemoryMethod)) {
            return false;
        }
        return ASMUtil.isSameMethod(((MemoryMethod) obj).methodNode, methodNode);
    }

}
