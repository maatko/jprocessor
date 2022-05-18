package me.mat.jprocessor.util.asm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.tree.MethodNode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ASMUtil {

    /**
     * Checks if the provided method nodes are the same
     *
     * @param first  first method node that you want to check
     * @param second second method node that you want to check
     * @return {@link Boolean}
     */

    public static boolean isSameMethod(MethodNode first, MethodNode second) {
        return first.name.equals(second.name) && first.desc.equals(second.desc) && first.access == second.access;
    }

}
