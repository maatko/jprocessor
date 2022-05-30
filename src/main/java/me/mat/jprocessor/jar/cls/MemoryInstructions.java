package me.mat.jprocessor.jar.cls;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

@RequiredArgsConstructor
public class MemoryInstructions {

    private final MemoryMethod memoryMethod;

    private final InsnList instructions;

    public MemoryInstructions() {
        this(null, new InsnList());
    }

    public MethodInsnNode createInvoke(MemoryMethod memoryMethod) {
        return new MethodInsnNode(
                memoryMethod.isStatic() ? Opcodes.INVOKESTATIC : Opcodes.INVOKESPECIAL,
                memoryMethod.parent.name(),
                memoryMethod.name(),
                memoryMethod.description()
        );
    }

    public void addInto(MemoryInstructions instructions, boolean clear) {
        if (clear) {
            instructions.clear();
        }
        for (AbstractInsnNode instruction : this.instructions) {
            instructions.instructions.add(instruction);
        }
    }

    public void insertInto(MemoryInstructions instructions, boolean clear) {
        if (clear) {
            instructions.clear();
        }
        for (AbstractInsnNode instruction : this.instructions) {
            instructions.instructions.insert(instruction);
        }
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

}
