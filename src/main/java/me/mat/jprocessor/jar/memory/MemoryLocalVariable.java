package me.mat.jprocessor.jar.memory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

@RequiredArgsConstructor
public class MemoryLocalVariable {

    @NonNull
    private LocalVariableNode localVariableNode;

    /**
     * Gets the name of the local variable
     *
     * @return {@link String}
     */

    public String name() {
        return localVariableNode.name;
    }

    /**
     * Sets the name of the local variable
     *
     * @param name name that you want to set it to
     */

    public void setName(String name) {
        localVariableNode.name = name;
    }

    /**
     * Gets the description of the variable
     *
     * @return {@link String}
     */

    public String desc() {
        return localVariableNode.desc;
    }

    /**
     * Gets the signature of the variable
     *
     * @return {@link String}
     */

    public String signature() {
        return localVariableNode.signature;
    }

    /**
     * Gets the starting label node that the variable is in
     *
     * @return {@link LabelNode}
     */

    public LabelNode start() {
        return localVariableNode.start;
    }

    /**
     * Gets the ending label node that the variable is in
     *
     * @return {@link LabelNode}
     */

    public LabelNode end() {
        return localVariableNode.end;
    }

    /**
     * Gets the index of the current local variable
     *
     * @return {@link Integer}
     */

    public int index() {
        return localVariableNode.index;
    }

}
