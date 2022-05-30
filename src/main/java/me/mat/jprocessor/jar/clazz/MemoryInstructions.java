package me.mat.jprocessor.jar.clazz;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@RequiredArgsConstructor
public class MemoryInstructions {

    private final MemoryMethod memoryMethod;

    private final InsnList instructions;

    public MemoryInstructions() {
        this(null, new InsnList());
    }

    /**
     * Adds a get field instruction to the list of instructions
     *
     * @param memoryField field that you want to add the instruction for
     */

    public void addGet(MemoryField memoryField) {
        // create the get instruction for the field
        FieldInsnNode fieldInsnNode = createGet(memoryField);

        // if the instruction is get field
        if (fieldInsnNode.getOpcode() == Opcodes.GETFIELD) {

            // load the instance of the object first
            add(new VarInsnNode(Opcodes.ALOAD, 0));
        }

        // then add the get instruction
        add(fieldInsnNode);
    }

    /**
     * Inserts a get field instruction to the list of instructions
     *
     * @param memoryField field that you want to insert the instruction for
     */

    public void insertGet(MemoryField memoryField) {
        // create the get instruction for the field
        FieldInsnNode fieldInsnNode = createGet(memoryField);

        // if the instruction is get field
        if (fieldInsnNode.getOpcode() == Opcodes.GETFIELD) {

            // load the instance of the object first
            insert(new VarInsnNode(Opcodes.ALOAD, 0));
        }

        // then add the get instruction
        insert(fieldInsnNode);
    }

    /**
     * Adds an invoke instruction for the provided
     * method into the current instructions list
     *
     * @param memoryMethod method that you want to invoke
     */

    public void addInvoke(MemoryMethod memoryMethod) {
        // create the method invoke instruction
        MethodInsnNode methodInsnNode = createInvoke(memoryMethod);

        // if the instruction is not a static invoke
        if (methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL) {

            // get the instance of the class on top of the stack
            add(new VarInsnNode(Opcodes.ALOAD, 0));
        }

        // then add the method invoke instruction
        add(methodInsnNode);
    }

    /**
     * Inserts an invoke instruction for the provided
     * method into the current instructions list
     *
     * @param memoryMethod method that you want to invoke
     */

    public void insertInvoke(MemoryMethod memoryMethod) {
        // create the method invoke instruction
        MethodInsnNode methodInsnNode = createInvoke(memoryMethod);

        // if the instruction is not a static invoke
        if (methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL) {

            // insert the instance of the class on top of the stack
            insert(new VarInsnNode(Opcodes.ALOAD, 0));
        }

        // then insert the method invoke instruction
        insert(methodInsnNode);
    }

    /**
     * Adds an instruction to the current instruction list
     *
     * @param instruction instruction that you want to add
     */

    public void add(AbstractInsnNode instruction) {
        instructions.add(instruction);
    }

    /**
     * Adds a list of instructions to the current list of instructions
     *
     * @param instructions list of instructions that you want to add
     */

    public void add(MemoryInstructions instructions) {
        instructions.addInto(this, false);
    }

    /**
     * Inserts an instruction into the instructions list
     *
     * @param instruction instruction that you want to insert
     */

    public void insert(AbstractInsnNode instruction) {
        instructions.insert(instruction);
    }

    /**
     * Inserts a list of instruction into the current instructions
     *
     * @param instructions instructions that you want to insert
     */

    public void insert(MemoryInstructions instructions) {
        instructions.insertInto(this, false);
    }

    /**
     * Inserts an instruction after an instruction
     *
     * @param targetInstruction instruction that you want to insert after
     * @param instruction       instruction that you are inserting
     */

    public void insertAfter(AbstractInsnNode targetInstruction, AbstractInsnNode instruction) {
        instructions.insert(targetInstruction, instruction);
    }

    /**
     * inserts a list of instructions after an instruction
     *
     * @param targetInstruction instruction that you want to insert after
     * @param instructions      instruction that you are inserting
     */

    public void insertAfter(AbstractInsnNode targetInstruction, MemoryInstructions instructions) {
        this.instructions.insert(targetInstruction, instructions.instructions);
    }

    /**
     * Inserts an instruction before an instruction
     *
     * @param targetInstruction instruction that you want to insert before
     * @param instruction       instruction that you want to insert
     */

    public void insertBefore(AbstractInsnNode targetInstruction, AbstractInsnNode instruction) {
        instructions.insertBefore(targetInstruction, instruction);
    }

    /**
     * Inserts a list of instructions before an instruction
     *
     * @param targetInstruction instruction that you want to insert before
     * @param instructions      instructions that you want to insert
     */

    public void insertBefore(AbstractInsnNode targetInstruction, MemoryInstructions instructions) {
        this.instructions.insertBefore(targetInstruction, instructions.instructions);
    }

    /**
     * Clears all the instructions
     */

    public void clear() {
        this.instructions.clear();
    }

    /**
     * Adds all the instructions to the provided list of instructions
     *
     * @param instructions list of instructions that you want to add to
     * @param clear        flag if the instructions list should be cleared before adding to it
     */

    public void addInto(MemoryInstructions instructions, boolean clear) {
        // if the clear flag is set
        if (clear) {

            // clear all the instructions
            instructions.clear();
        }

        // loop through all the current instructions
        for (AbstractInsnNode instruction : this.instructions) {

            // and copy every single instructions into the new instruction list
            instructions.instructions.add(instruction);
        }
    }

    /**
     * Inserts all the instructions to the provided list of instructions
     *
     * @param instructions list of instructions that you want to add to
     * @param clear        flag if the instructions list should be cleared before adding to it
     */

    public void insertInto(MemoryInstructions instructions, boolean clear) {
        // if the clear flag is set
        if (clear) {

            // clear all the instructions
            instructions.clear();
        }

        // loop through all the current instructions
        for (AbstractInsnNode instruction : this.instructions) {

            // and copy every single instructions into the new instruction list
            instructions.instructions.insert(instruction);
        }
    }

    /**
     * Creates an invoke instruction for the provided method
     *
     * @param memoryMethod method that you want to create the instruction for
     * @return {@link MethodInsnNode}
     */

    private MethodInsnNode createInvoke(MemoryMethod memoryMethod) {
        return new MethodInsnNode(
                memoryMethod.isStatic() ? Opcodes.INVOKESTATIC : Opcodes.INVOKESPECIAL,
                memoryMethod.parent.name(),
                memoryMethod.name(),
                memoryMethod.description()
        );
    }

    /**
     * Creates a get instruction for the provided field
     *
     * @param memoryField field that you want to create the get instruction
     * @return {@link FieldInsnNode}
     */

    private FieldInsnNode createGet(MemoryField memoryField) {
        return new FieldInsnNode(
                memoryField.isStatic() ? Opcodes.GETSTATIC : Opcodes.GETFIELD,
                memoryField.parent.name(),
                memoryField.name(),
                memoryField.description()
        );
    }

}
