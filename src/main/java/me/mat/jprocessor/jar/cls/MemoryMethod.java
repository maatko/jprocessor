package me.mat.jprocessor.jar.cls;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.util.asm.ASMUtil;
import org.objectweb.asm.tree.MethodNode;

@RequiredArgsConstructor
public class MemoryMethod {

    @NonNull
    public MethodNode methodNode;

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
