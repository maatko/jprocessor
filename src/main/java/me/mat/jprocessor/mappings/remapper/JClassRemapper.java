package me.mat.jprocessor.mappings.remapper;

import me.mat.jprocessor.jar.memory.MemoryJar;
import me.mat.jprocessor.mappings.MappingManager;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.ClassRemapper;

public class JClassRemapper extends ClassRemapper {

    private final MemoryJar memoryJar;

    public JClassRemapper(ClassVisitor classVisitor, MemoryJar memoryJar, MappingManager mappingManager) {
        super(classVisitor, mappingManager);
        this.memoryJar = memoryJar;
    }

    @Override
    protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
        return new JMethodRemapper(this.api, methodVisitor, memoryJar, this.remapper);
    }

}
