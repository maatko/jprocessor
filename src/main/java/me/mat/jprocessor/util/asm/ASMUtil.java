package me.mat.jprocessor.util.asm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.tree.FieldNode;
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


    /**
     * Checks if the provided field nodes are the same
     *
     * @param first  first field node that you want to check
     * @param second second field node that you want to check
     * @return {@link Boolean}
     */

    public static boolean isSameField(FieldNode first, FieldNode second) {
        return first.name.equals(second.name) && first.desc.equals(second.desc) && first.access == second.access;
    }

    /**
     * Turns a java type into a asm type
     *
     * @param type type that you want to convert
     * @return {@link String}
     */

    public static String toByteCodeFromJava(String type) {
        switch (type.toLowerCase()) {
            case "void":
                return "V";
            case "boolean":
                return "Z";
            case "byte":
                return "B";
            case "char":
                return "C";
            case "short":
                return "S";
            case "int":
                return "I";
            case "long":
                return "J";
            case "float":
                return "F";
            case "double":
                return "D";
            default:
                return type;
        }
    }

}
