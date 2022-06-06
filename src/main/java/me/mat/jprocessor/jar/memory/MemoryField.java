package me.mat.jprocessor.jar.memory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.util.asm.ASMUtil;
import me.mat.jprocessor.util.asm.IAccessed;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;

import java.util.Map;

@RequiredArgsConstructor
public class MemoryField extends MemoryAnnotatedElement implements IAccessed {

    @NonNull
    public MemoryClass parent;

    @NonNull
    private FieldNode fieldNode;

    /**
     * Loads all the annotation for the current field
     *
     * @param classes map of all the loaded classes
     * @return {@link MemoryField}
     */

    public MemoryField init(Map<String, MemoryClass> classes) {
        // initialize all the annotations
        this.init(fieldNode.visibleAnnotations, classes);

        // return the instance of the field
        return this;
    }

    /**
     * Gets the name of the field
     *
     * @return @{@link String}
     */

    public String name() {
        return fieldNode.name;
    }

    /**
     * Gets the description of the field
     *
     * @return @{@link String}
     */

    public String description() {
        return fieldNode.desc;
    }

    /**
     * Gets the access of the field
     *
     * @return {@link Integer}
     */

    public int getAccess() {
        return fieldNode.access;
    }

    /**
     * Sets the access of the field
     *
     * @param access access that you want to set it to
     */

    public void setAccess(int access) {
        fieldNode.access = access;
    }

    /**
     * Checks if the instruction matches
     * the current field
     *
     * @param instruction instruction that you want to check against
     * @return {@link Boolean}
     */

    public boolean isCorrectInstruction(int instruction) {
        if (instruction == Opcodes.GETSTATIC && !isStatic()) {
            return false;
        } else if (instruction == Opcodes.PUTSTATIC && (!isStatic() || isFinal())) {
            return false;
        } else if (instruction == Opcodes.GETFIELD && isStatic()) {
            return false;
        } else return instruction != Opcodes.PUTFIELD || (!isStatic() && !isFinal());
    }

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
