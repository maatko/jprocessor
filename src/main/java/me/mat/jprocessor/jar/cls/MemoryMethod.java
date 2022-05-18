package me.mat.jprocessor.jar.cls;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.util.asm.ASMUtil;
import org.objectweb.asm.tree.MethodNode;

@RequiredArgsConstructor
public class MemoryMethod {

    @NonNull
    public MethodNode methodNode;

    public OverrideMethod originalMethod;

    /**
     * Checks if this method is an override method from previous parent classes
     *
     * @return {@link Boolean}
     */

    public boolean isOverride() {
        return originalMethod != null;
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

    public static final class OverrideMethod extends MemoryMethod {

        @NonNull
        public MemoryClass parentClass;

        public OverrideMethod(@NonNull MemoryClass parentClass, @NonNull MethodNode methodNode) {
            super(methodNode);
            this.parentClass = parentClass;
        }

    }

}
