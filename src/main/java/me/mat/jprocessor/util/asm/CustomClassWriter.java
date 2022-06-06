package me.mat.jprocessor.util.asm;

import org.objectweb.asm.ClassWriter;

public class CustomClassWriter extends ClassWriter {

    public CustomClassWriter(int flags) {
        super(flags);
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
