package me.mat.jprocessor;

import lombok.Setter;
import me.mat.jprocessor.jar.JarLoadCallback;
import me.mat.jprocessor.jar.memory.MemoryJar;
import me.mat.jprocessor.mappings.MappingLoadCallback;
import me.mat.jprocessor.mappings.MappingLoadException;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.MappingType;
import me.mat.jprocessor.mappings.generation.GenerationType;
import me.mat.jprocessor.mappings.generation.MappingGenerateCallback;
import me.mat.jprocessor.mappings.generation.MappingGenerateException;
import me.mat.jprocessor.mappings.generation.generator.MappingGenerator;
import me.mat.jprocessor.util.log.ConsoleLoggerImpl;
import me.mat.jprocessor.util.log.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JProcessor {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);

    public static final class Jar {

        /**
         * Loads a memory jar from the provided classes
         *
         * @param classes   classes that you want to load
         * @param resources resources that you want to load
         * @return {@link MemoryJar}
         */

        public static MemoryJar load(Map<String, byte[]> classes, Map<String, byte[]> resources, String mainClass) {
            return new MemoryJar(classes, resources, mainClass);
        }

        /**
         * Loads a memory jar from the provided classes asynchronously
         *
         * @param classes classes that you want to load
         */

        public static void load(Map<String, byte[]> classes, Map<String, byte[]> resources, String mainClass, JarLoadCallback callback) {
            EXECUTOR_SERVICE.submit(() -> callback.onLoad(load(classes, resources, mainClass)));
        }

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

    }

    public static final class Mapping {

        /**
         * Generates mappings for the loaded memory jar
         * based on the provided generation type
         *
         * @param generationType type of the mapping generator that you want to use
         * @param memoryJar      jar that you want to generate mappings for
         * @return {@link MappingManager}
         * @throws MappingGenerateException
         */

        public static MappingManager generate(GenerationType generationType, MemoryJar memoryJar) throws MappingGenerateException {
            return generate(generationType.getMappingGenerator(), memoryJar);
        }

        /**
         * Generates mappings for the loaded memory jar
         * based on the provided generation type
         *
         * @param generator generate that you want to use to generate the mappings
         * @param memoryJar jar that you want to generate mappings for
         * @return {@link MappingManager}
         * @throws MappingGenerateException
         */

        public static MappingManager generate(MappingGenerator generator, MemoryJar memoryJar) throws MappingGenerateException {
            return new MappingManager(generator, memoryJar);
        }

        /**
         * Generates mappings for the loaded memory jar
         * asynchronously based on the provided generation type
         *
         * @param mappingGenerator generator that you want to use to generate mappings for
         * @param memoryJar        jar that you want to generate mappings for
         */

        public static void generate(MappingGenerator mappingGenerator, MemoryJar memoryJar, MappingGenerateCallback callback) {
            EXECUTOR_SERVICE.submit(() -> {
                try {
                    callback.onFinish(generate(mappingGenerator, memoryJar));
                } catch (MappingGenerateException e) {
                    callback.onFail(e.getMessage());
                }
            });
        }

        /**
         * Generates mappings for the loaded memory jar
         * asynchronously based on the provided generation type
         *
         * @param generationType type of mappings that you want to generate
         * @param memoryJar      jar that you want to generate mappings for
         */

        public static void generate(GenerationType generationType, MemoryJar memoryJar, MappingGenerateCallback callback) {
            generate(generationType.getMappingGenerator(), memoryJar, callback);
        }

        /**
         * Loads mappings from a file
         * based on the provided mapping type
         *
         * @param memoryJar   jar that has been loaded in memory
         * @param file        file that the mappings are contained in
         * @param mappingType type of the mappings that the file is saved in
         * @return {@link MappingManager}
         * @throws MappingLoadException
         */

        public static MappingManager load(MemoryJar memoryJar, File file, MappingType mappingType) throws MappingLoadException {
            return new MappingManager(mappingType.getProcessor(), file, memoryJar);
        }

        /**
         * Loads mappings from a file asynchronously
         * based on the provided mapping type
         *
         * @param memoryJar   jar that has been loaded in memory
         * @param file        file that the mappings are contained in
         * @param mappingType type of the mappings that the file is saved in
         * @return {@link MappingManager}
         * @throws MappingLoadException
         */

        public static void load(MemoryJar memoryJar, File file, MappingType mappingType, MappingLoadCallback callback) {
            EXECUTOR_SERVICE.submit(() -> {
                try {
                    callback.onLoad(load(memoryJar, file, mappingType));
                } catch (MappingLoadException e) {
                    callback.onFail(e.getMessage());
                }
            });
        }

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
