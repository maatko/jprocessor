package me.mat.jprocessor.util.asm;

import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;

public interface IAccessed {

    int SYNTHETIC = 0x00001000;
    int MANDATED = 0x00008000;

    int getAccess();

    void setAccess(int access);

    default boolean isPublic() {
        return Modifier.isPublic(getAccess());
    }

    default boolean isPrivate() {
        return Modifier.isPrivate(getAccess());
    }

    default boolean isProtected() {
        return Modifier.isProtected(getAccess());
    }

    default boolean isStatic() {
        return Modifier.isStatic(getAccess());
    }

    default boolean isFinal() {
        return Modifier.isFinal(getAccess());
    }

    default boolean isSynchronized() {
        return Modifier.isSynchronized(getAccess());
    }

    default boolean isVolatile() {
        return Modifier.isTransient(getAccess());
    }

    default boolean isTransient() {
        return Modifier.isTransient(getAccess());
    }

    default boolean isNative() {
        return Modifier.isNative(getAccess());
    }

    default boolean isInterface() {
        return Modifier.isInterface(getAccess());
    }

    default boolean isAbstract() {
        return Modifier.isAbstract(getAccess());
    }

    default boolean isStrict() {
        return Modifier.isStrict(getAccess());
    }

    default boolean isEnum() {
        return hasModifier(Opcodes.ACC_ENUM);
    }

    default boolean isAnnotation() {
        return isInterface() && hasAnnotation();
    }

    default boolean hasModifier(int modifier) {
        return (getAccess() & modifier) != 0;
    }

    default boolean hasAnnotation() {
        return hasModifier(Opcodes.ACC_ANNOTATION);
    }

    default boolean isSynthetic() {
        return hasModifier(SYNTHETIC);
    }

    default boolean isMandated() {
        return hasModifier(MANDATED);
    }

}
