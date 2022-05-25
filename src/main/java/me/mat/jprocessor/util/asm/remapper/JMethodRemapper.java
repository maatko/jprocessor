package me.mat.jprocessor.util.asm.remapper;

import me.mat.jprocessor.jar.MemoryJar;
import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.jar.cls.MemoryField;
import me.mat.jprocessor.jar.cls.MemoryMethod;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.mapping.FieldMapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;

import java.util.concurrent.atomic.AtomicReference;

public class JMethodRemapper extends MethodRemapper {

    private final MemoryJar memoryJar;

    protected JMethodRemapper(int api, MethodVisitor methodVisitor, MemoryJar memoryJar, Remapper remapper) {
        super(api, methodVisitor, remapper);
        this.memoryJar = memoryJar;
    }

    @Override
    public void visitMethodInsn(int opcodeAndSource, String owner, String name, String descriptor, boolean isInterface) {
        if (this.api < 327680 && (opcodeAndSource & 256) == 0) {
            super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
        } else {
            // get the method mapping
            String methodMapping = this.remapper.mapMethodName(owner, name, descriptor);

            // if the mapping is the same as name that means it needs to be searched for in super methods
            if (methodMapping.equals(name)) {

                // get the MemoryClass of the owner
                MemoryClass memoryClass = memoryJar.getClass(owner);

                // make sure that its valid
                if (memoryClass != null) {

                    // find the method in the super methods
                    AtomicReference<MemoryClass> classReference = new AtomicReference<>(null);
                    AtomicReference<MemoryMethod> methodReference = new AtomicReference<>(null);
                    memoryClass.getMethod(name, descriptor, classReference, methodReference);

                    // get the method and the super class
                    MemoryMethod method = methodReference.get();
                    MemoryClass superClass = classReference.get();

                    // make sure that the super class and method are valid
                    if (method != null && superClass != null) {

                        // get the mapping
                        MethodMapping mapping = ((MappingManager) remapper).getMethodByMapping(
                                this.remapper.mapType(superClass.name()),
                                method.name(),
                                method.description()
                        );

                        // make sure that the mapping is valid
                        if (mapping != null) {

                            // update the method mapping
                            methodMapping = mapping.name;
                        }
                    }
                }
            }

            // pass the visit call and remap the method call
            super.visitMethodInsn(
                    opcodeAndSource,
                    this.remapper.mapType(owner),
                    methodMapping,
                    this.remapper.mapMethodDesc(descriptor),
                    isInterface
            );
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        // get the field mapping
        String fieldMapping = this.remapper.mapFieldName(owner, name, descriptor);

        // if the mapping is the same as name that means it needs to be searched for in super fields
        if (name.equals(fieldMapping)) {

            // get the MemoryClass of the owner
            MemoryClass memoryClass = memoryJar.getClass(owner);

            // make sure that its valid
            if (memoryClass != null) {

                // find the field from one of the super classes
                AtomicReference<MemoryClass> classReference = new AtomicReference<>(null);
                AtomicReference<MemoryField> fieldReference = new AtomicReference<>(null);
                memoryClass.findField(opcode, name, descriptor, classReference, fieldReference);

                // get the field and the super class
                MemoryField field = fieldReference.get();
                MemoryClass superClass = classReference.get();

                // make sure that the super class and the field are valid
                if (field != null && superClass != null) {

                    // get the mapping
                    FieldMapping mapping = ((MappingManager) remapper).getFieldByMapping(
                            this.remapper.mapType(superClass.name()),
                            field.name(),
                            field.description()
                    );

                    // make sure that the mapping is valid
                    if (mapping != null) {

                        // update the field mapping
                        fieldMapping = mapping.name;
                    }
                }
            }
        }

        // pass the visit call and map the field
        super.visitFieldInsn(
                opcode,
                this.remapper.mapType(owner),
                fieldMapping,
                this.remapper.mapDesc(descriptor)
        );
    }

}
