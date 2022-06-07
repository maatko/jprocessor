package me.mat.jprocessor.jar.memory;

import lombok.NonNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;

public class MemoryInstructions {

    private final Map<AbstractInsnNode, LabelNode> labelLookupTable = new HashMap<>();

    private final MemoryMethod memoryMethod;

    @NonNull
    private final InsnList instructions;

    public MemoryInstructions(MemoryMethod memoryMethod, InsnList instructions) {
        this.memoryMethod = memoryMethod;
        this.instructions = instructions;

        // setup the lookup table
        this.setupLabelLookupTable();
    }

    public MemoryInstructions() {
        this(null, new InsnList());
    }

    /**
     * Sets up the label lookup table with the correct labels
     */

    private void setupLabelLookupTable() {
        // clear the lookup table
        labelLookupTable.clear();

        // loop through all the instructions
        instructions.forEach(instruction -> {

            // if the instruction is not a label instruction
            if (!(instruction instanceof LabelNode)) {

                // find the label node that the instruction is in
                LabelNode labelNode = findLabel(instruction);

                // if the label node was found
                if (labelNode != null) {

                    // cache it to the lookup table
                    labelLookupTable.put(instruction, labelNode);
                }
            }
        });
    }

    /**
     * Finds a label node for the
     * provided instruction
     *
     * @param instruction instruction that you want to get the label node for
     * @return {@link LabelNode}
     */

    private LabelNode findLabel(AbstractInsnNode instruction) {
        // get the index of the instruction
        int index = indexOf(instruction);

        // if the index is not found
        if (index == -1) {

            // return null
            return null;
        }

        // loop from the instruction backwards
        for (int i = index; i > 0; i--) {

            // get the abstract node
            AbstractInsnNode abstractInsnNode = get(i);

            // if the instruction is a label node
            if (abstractInsnNode instanceof LabelNode) {

                // return it
                return (LabelNode) abstractInsnNode;
            }
        }

        // else return null
        return null;
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
        // add the instruction
        instructions.add(instruction);

        // and setup the label lookup table
        setupLabelLookupTable();
    }

    /**
     * Adds a list of instructions to the current list of instructions
     *
     * @param instructions list of instructions that you want to add
     */

    public void add(MemoryInstructions instructions) {
        // add the instruction list
        instructions.addInto(this, false);

        // and setup the label lookup table
        setupLabelLookupTable();
    }

    /**
     * Inserts an instruction into the instructions list
     *
     * @param instruction instruction that you want to insert
     */

    public void insert(AbstractInsnNode instruction) {
        // insert the instruction
        instructions.insert(instruction);

        // and setup the label lookup table
        setupLabelLookupTable();
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
        // insert the instruction
        instructions.insert(targetInstruction, instruction);

        // setup the lookup table
        setupLabelLookupTable();
    }

    /**
     * inserts a list of instructions after an instruction
     *
     * @param targetInstruction instruction that you want to insert after
     * @param instructions      instruction that you are inserting
     */

    public void insertAfter(AbstractInsnNode targetInstruction, MemoryInstructions instructions) {
        // insert the instructions
        this.instructions.insert(targetInstruction, instructions.instructions);

        // setup the lookup table
        setupLabelLookupTable();
    }

    /**
     * Inserts an instruction before an instruction
     *
     * @param targetInstruction instruction that you want to insert before
     * @param instruction       instruction that you want to insert
     */

    public void insertBefore(AbstractInsnNode targetInstruction, AbstractInsnNode instruction) {
        // insert the instruction
        instructions.insertBefore(targetInstruction, instruction);

        // setup the lookup table
        setupLabelLookupTable();
    }

    /**
     * Inserts a list of instructions before an instruction
     *
     * @param targetInstruction instruction that you want to insert before
     * @param instructions      instructions that you want to insert
     */

    public void insertBefore(AbstractInsnNode targetInstruction, MemoryInstructions instructions) {
        // insert the instructions
        this.instructions.insertBefore(targetInstruction, instructions.instructions);

        // setup the lookup table
        setupLabelLookupTable();
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

        // setup the label lookup table
        instructions.setupLabelLookupTable();
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

        // setup the label lookup table
        instructions.setupLabelLookupTable();
    }

    /**
     * Gets the first instruction in the list
     *
     * @return {@link AbstractInsnNode}
     */

    public AbstractInsnNode getFirst() {
        return instructions.getFirst();
    }

    /**
     * Gets an instruction from the list
     * based on the provided index
     *
     * @param index index of the instruction that you want to get
     * @return {@link AbstractInsnNode}
     */

    public AbstractInsnNode get(int index) {
        return instructions.get(index);
    }

    /**
     * Returns the index of the provided instruction
     *
     * @param abstractInsnNode instruction that you want to get the index for
     * @return {@link Integer}
     */

    public int indexOf(AbstractInsnNode abstractInsnNode) {
        return instructions.indexOf(abstractInsnNode);
    }

    /**
     * Gets the last instruction in the list
     *
     * @return {@link AbstractInsnNode}
     */

    public AbstractInsnNode getLast() {
        return instructions.getLast();
    }

    /**
     * Returns the size of the instructions list
     *
     * @return {@link Integer}
     */

    public int size() {
        return instructions.size();
    }

    /**
     * Gets the label for the provided instruction
     *
     * @param instruction instruction that you want to get the label for
     * @return {@link LabelNode}
     */

    public LabelNode getLabelForInstruction(AbstractInsnNode instruction) {
        return labelLookupTable.getOrDefault(instruction, null);
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
