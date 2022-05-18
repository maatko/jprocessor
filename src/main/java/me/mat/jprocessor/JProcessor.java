package me.mat.jprocessor;

import lombok.Setter;
import me.mat.jprocessor.jar.MemoryJar;
import me.mat.jprocessor.util.log.Logger;
import me.mat.jprocessor.util.log.ConsoleLoggerImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JProcessor {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);

    /**
     * Loads a jar into the memory from the provided path
     *
     * @param path path to the jar file
     * @return {@link MemoryJar}
     */

    public static MemoryJar load(String path) throws FileNotFoundException {
        return load(new File(path));
    }

    /**
     * Loads a jar into the memory asynchronously from the provided file
     *
     * @param path     path to the jar file
     * @param callback callback of the load action
     */

    public static void load(String path, JarLoadCallback callback) {
        EXECUTOR_SERVICE.submit(() -> {
            try {
                callback.onLoad(load(path));
            } catch (FileNotFoundException e) {
                callback.onFail(e.getMessage());
            }
        });
    }

    /**
     * Loads a jar into the memory from the provided file
     *
     * @param file file handle of the jar
     * @return {@link MemoryJar}
     */

    public static MemoryJar load(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException("File '" + file.getAbsolutePath() + "' does not exist");
        }
        return new MemoryJar(file);
    }

    /**
     * Loads a jar into the memory asynchronously from the provided file
     *
     * @param file     file handle of the jar
     * @param callback callback of the load action
     */

    public static void load(File file, JarLoadCallback callback) {
        EXECUTOR_SERVICE.submit(() -> {
            try {
                callback.onLoad(load(file));
            } catch (FileNotFoundException e) {
                callback.onFail(e.getMessage());
            }
        });
    }

    public static final class Logging {

        @Setter
        private static Logger handle = new ConsoleLoggerImpl();

        /**
         * Prints an information message to the provided logger
         *
         * @param format format of the message
         * @param args   arguments of the message
         */

        public static void info(String format, Object... args) {
            if (handle != null) {
                handle.info(format, args);
            }
        }

        /**
         * Prints a warning message to the provided logger
         *
         * @param format format of the message
         * @param args   arguments of the message
         */

        public static void warn(String format, Object... args) {
            if (handle != null) {
                handle.warn(format, args);
            }
        }

        /**
         * Prints an error message to the provided logger
         *
         * @param format format of the message
         * @param args   arguments of the message
         */

        public static void error(String format, Object... args) {
            if (handle != null) {
                handle.error(format, args);
            }
        }

    }

}
