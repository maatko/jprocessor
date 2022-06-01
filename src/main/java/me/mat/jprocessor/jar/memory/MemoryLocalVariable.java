package me.mat.jprocessor.jar.memory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

@RequiredArgsConstructor
public class MemoryLocalVariable {

    @NonNull
    private LocalVariableNode localVariableNode;

    public String name() {
        return localVariableNode.name;
    }

    public void setName(String name) {
        localVariableNode.name = name;
    }

    public String desc() {
        return localVariableNode.desc;
    }

    public String signature() {
        return localVariableNode.signature;
    }

    public LabelNode start() {
        return localVariableNode.start;
    }

    public LabelNode end() {
        return localVariableNode.end;
    }

    public int index() {
        return localVariableNode.index;
    }

}
