package me.mat.jprocessor.util.asm;

import me.mat.jprocessor.jar.MemoryJar;
import org.objectweb.asm.ClassWriter;

public class CustomClassWriter extends ClassWriter {

    private final MemoryJar memoryJar;

    public CustomClassWriter(MemoryJar memoryJar, int flags) {
        super(flags);
        this.memoryJar = memoryJar;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        try {
            return super.getCommonSuperClass(type1, type2);
        } catch (TypeNotPresentException e) {
            return type1;
        }
    }

}
