package me.mat.jprocessor.jar.cls;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.util.asm.ASMUtil;
import org.objectweb.asm.tree.FieldNode;

@RequiredArgsConstructor
public class MemoryField {

    @NonNull
    public FieldNode fieldNode;

    /**
     * Checks if the provided object is equal to this object
     *
     * @param obj object that you are trying to check
     * @return {@link Boolean}
     */

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldNode) {
            return ASMUtil.isSameField((FieldNode) obj, fieldNode);
        } else if (!(obj instanceof MemoryField)) {
            return false;
        }
        return ASMUtil.isSameField(((MemoryField) obj).fieldNode, fieldNode);
    }

}
