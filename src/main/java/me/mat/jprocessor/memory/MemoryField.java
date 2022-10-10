package me.mat.jprocessor.memory;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.FieldNode;

@Getter
@RequiredArgsConstructor
public class MemoryField {

    @NonNull
    private final FieldNode fieldNode;

    /**
     * Gets the name of the {@link MemoryField}
     *
     * @return {@link String} name of the field
     */

    public String getName() {
        return fieldNode.name;
    }

    /**
     * Gets the signature of the {@link MemoryField}
     *
     * @return {@link String} signature of the field
     */

    public String getSig() {
        return fieldNode.signature;
    }

    /**
     * Gets the description of the {@link MemoryField}
     *
     * @return {@link String} description of the field
     */

    public String getDesc() {
        return fieldNode.desc;
    }

}
