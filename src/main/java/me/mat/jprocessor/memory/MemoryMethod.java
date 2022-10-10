package me.mat.jprocessor.memory;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.MethodNode;

@Getter
@RequiredArgsConstructor
public class MemoryMethod {

    @NonNull
    private final MethodNode methodNode;

    public MemoryClass superClass;
    public MemoryMethod superMethod;

    /**
     * Gets the name of the {@link MemoryMethod}
     *
     * @return {@link String} name of the method
     */

    public String getName() {
        return methodNode.name;
    }

    /**
     * Gets the signature of the {@link MemoryMethod}
     *
     * @return {@link String} signature of the method
     */

    public String getSig() {
        return methodNode.signature;
    }

    /**
     * Gets the description of the {@link MemoryMethod}
     *
     * @return {@link String} description of the method
     */

    public String getDesc() {
        return methodNode.desc;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MemoryMethod)) {
            return false;
        }
        MemoryMethod memoryMethod = (MemoryMethod) obj;
        return (memoryMethod.getName().equals(getName()) && memoryMethod.getDesc().equals(getDesc())) || super.equals(obj);
    }

}
