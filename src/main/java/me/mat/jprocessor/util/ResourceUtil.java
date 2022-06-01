package me.mat.jprocessor.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceUtil {

    public static InputStream getClassResource(Class<?> aClass) {
        return getResource("/" + aClass.getName().replaceAll("\\.", "/") + ".class");
    }

    /**
     * Gets a resource from the provided path
     *
     * @param path path of the resource
     *
     * @return {@link InputStream}
     */

    public static InputStream getResource(String path) {
        return ResourceUtil.class.getResourceAsStream(path);
    }

}
