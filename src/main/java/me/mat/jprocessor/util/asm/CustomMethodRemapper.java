package me.mat.jprocessor.util.asm;

import me.mat.jprocessor.jar.MemoryJar;
import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.jar.cls.MemoryField;
import me.mat.jprocessor.jar.cls.MemoryMethod;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.mapping.FieldMapping;
import me.mat.jprocessor.mappings.mapping.MethodMapping;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CustomMethodRemapper extends MethodRemapper {

    private final MemoryJar memoryJar;

    protected CustomMethodRemapper(int api, MethodVisitor methodVisitor, MemoryJar memoryJar, Remapper remapper) {
        super(api, methodVisitor, remapper);
        this.memoryJar = memoryJar;
    }

    @Override
    public void visitMethodInsn(int opcodeAndSource, String owner, String name, String descriptor, boolean isInterface) {
        if (this.api < 327680 && (opcodeAndSource & 256) == 0) {
            super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
        } else {
            String methodMapping = this.remapper.mapMethodName(owner, name, descriptor);
            if (methodMapping.equals(name)) {
                MemoryClass memoryClass = memoryJar.getClass(owner);
                if (memoryClass != null) {
                    AtomicReference<MemoryClass> classReference = new AtomicReference<>(null);
                    AtomicReference<MemoryMethod> methodReference = new AtomicReference<>(null);
                    Map<MemoryClass, List<MemoryMethod>> methodMap = new HashMap<>();
                    methodMap.put(memoryClass, memoryClass.methods);
                    methodMap.putAll(memoryClass.superMethods);
                    methodMap.forEach((superClass, methods) -> {
                        methods.stream().filter(memoryMethod -> memoryMethod.name().equals(name) && memoryMethod.description().equals(descriptor)).forEach(memoryMethod -> {
                            classReference.set(superClass);
                            methodReference.set(memoryMethod);
                        });
                    });
                    String classMapping = this.remapper.mapType(owner);
                    if (classMapping != null) {
                        MemoryMethod method = methodReference.get();
                        if (method != null) {
                            MemoryClass superClass = classReference.get();
                            MappingManager mappingManager = (MappingManager) remapper;
                            MethodMapping mapping = mappingManager.getMethodByMapping(
                                    this.remapper.mapType(superClass.name()),
                                    method.name(),
                                    method.description()
                            );
                            if (mapping != null) {
                                methodMapping = mapping.name;
                            }
                        }
                    }
                }
            }
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
        String fieldMapping = this.remapper.mapFieldName(owner, name, descriptor);
        if (name.equals(fieldMapping)) {
            MemoryClass memoryClass = memoryJar.getClass(owner);
            if (memoryClass != null) {
                AtomicReference<MemoryClass> classReference = new AtomicReference<>(null);
                AtomicReference<MemoryField> fieldReference = new AtomicReference<>(null);
                Map<MemoryClass, List<MemoryField>> fieldMap = new HashMap<>();
                fieldMap.put(memoryClass, memoryClass.fields);
                fieldMap.putAll(memoryClass.superFields);
                fieldMap.forEach((superClass, fields) -> {
                    for (MemoryField field : fields) {
                        if (opcode == Opcodes.GETSTATIC && !field.iStatic()) {
                            continue;
                        } else if (opcode == Opcodes.PUTSTATIC && (!field.iStatic() || field.isFinal())) {
                            continue;
                        } else if (opcode == Opcodes.GETFIELD && field.iStatic()) {
                            continue;
                        } else if (opcode == Opcodes.PUTFIELD && (field.iStatic() || field.isFinal())) {
                            continue;
                        }
                        if (field.name().equals(name) && field.description().equals(descriptor)) {
                            classReference.set(superClass);
                            fieldReference.set(field);
                        }
                    }
                });

                String classMapping = this.remapper.mapType(owner);
                if (classMapping != null) {
                    MemoryField field = fieldReference.get();
                    if (field != null) {
                        MemoryClass superClass = classReference.get();
                        MappingManager mappingManager = (MappingManager) remapper;
                        FieldMapping mapping = mappingManager.getFieldByMapping(
                                this.remapper.mapType(superClass.name()),
                                field.name(),
                                field.description()
                        );
                        if (mapping != null) {
                            fieldMapping = mapping.name;
                        }
                    }
                }
            }
        }
        super.visitFieldInsn(
                opcode,
                this.remapper.mapType(owner),
                fieldMapping,
                this.remapper.mapDesc(descriptor)
        );
    }

}
