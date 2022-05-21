package me.mat.jprocessor.util.asm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ASMUtil {

    private static final Map<String, String> TYPE_CONVERSION = new HashMap<>();

    private static final List<String> IGNORED = Arrays.asList(
            "valueOf",
            "values",
            "ordinal",
            "toString",
            "hashCode"
    );


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
     * @param javaType type that you want to convert
     * @return {@link String}
     */

    public static String toByteCodeFromJava(String javaType) {
        String type = javaType;
        StringBuilder prefix = new StringBuilder();
        if (type.contains("[")) {
            for (int i = 0; i < type.substring(type.indexOf("[")).length() / 2; i++) {
                prefix.append("[");
            }
            type = type.substring(0, type.indexOf("["));
        }
        return prefix + TYPE_CONVERSION.getOrDefault(type.toLowerCase(), !type.isEmpty() ? "L" + type + ";" : type);
    }


    /**
     * Checks if the method node
     * can be changed by the renamer
     *
     * @param methodNode that you want to check against
     * @return true/false depending if it can be changed or not
     */

    public static boolean isChangeable(MethodNode methodNode) {
        if (Modifier.isNative(methodNode.access))
            return false;
        if (methodNode.name.startsWith("<"))
            return false;
        if (methodNode.name.contains("$"))
            return false;
        if (IGNORED.contains(methodNode.name))
            return false;
        return true;
    }

    static {

        TYPE_CONVERSION.put("void", "V");
        TYPE_CONVERSION.put("boolean", "Z");
        TYPE_CONVERSION.put("byte", "B");
        TYPE_CONVERSION.put("char", "C");
        TYPE_CONVERSION.put("short", "S");
        TYPE_CONVERSION.put("int", "I");
        TYPE_CONVERSION.put("long", "J");
        TYPE_CONVERSION.put("float", "F");
        TYPE_CONVERSION.put("double", "D");

    }

}
