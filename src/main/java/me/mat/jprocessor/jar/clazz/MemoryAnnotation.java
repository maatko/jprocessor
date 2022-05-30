package me.mat.jprocessor.jar.clazz;

import lombok.NonNull;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryAnnotation {

    private final Map<String, Object> values = new HashMap<>();

    @NonNull
    private final AnnotationNode annotationNode;

    @NonNull
    public MemoryClass annotationClass;

    public MemoryAnnotation(@NonNull AnnotationNode annotationNode, @NonNull MemoryClass annotationClass) {
        this.annotationNode = annotationNode;
        this.annotationClass = annotationClass;

        // get the list of values
        List<Object> values = annotationNode.values;

        // if the values was not found
        if (values == null) {
            // return out of the method
            return;
        }

        // define a key that all the keys will be stored into
        String key = null;

        // loop through all the values
        for (int i = 0; i < values.size(); i++) {

            // get the current value
            Object value = values.get(i);

            // if the current index is matching the value index
            if (i > 0 && i % 2 != 0) {

                // load the value based on the key and the value
                this.values.put(key, value);
            } else {

                // else store the key as the current key
                key = value.toString();
            }
        }
    }

    /**
     * Gets the value of the ćannotation
     *
     * @return {@link Object}
     */

    public Object value() {
        return getValue("value");
    }

    /**
     * Checks if the current annotation contains the value
     *
     * @param key key that you want to check for
     * @return {@link Boolean}
     */

    public boolean hasValue(String key) {
        return values.containsKey(key);
    }

    /**
     * Gets the value from the annotation that matches the provided key
     *
     * @param key key of the value
     * @return {@link Object}
     */

    public Object getValue(String key) {
        return values.getOrDefault(key, null);
    }

    /**
     * Returns the description of the annotation node
     *
     * @return {@link String}
     */

    public String description() {
        return annotationNode.desc;
    }

}
