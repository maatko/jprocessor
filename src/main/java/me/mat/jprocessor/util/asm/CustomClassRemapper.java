package me.mat.jprocessor.util.asm;

import me.mat.jprocessor.jar.MemoryJar;
import me.mat.jprocessor.mappings.MappingManager;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.ClassRemapper;

public class CustomClassRemapper extends ClassRemapper {

    private final MemoryJar memoryJar;

    public CustomClassRemapper(ClassVisitor classVisitor, MemoryJar memoryJar, MappingManager mappingManager) {
        super(classVisitor, mappingManager);
        this.memoryJar = memoryJar;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
        return new CustomMethodRemapper(this.api, methodVisitor, memoryJar, this.remapper);
    }

}
