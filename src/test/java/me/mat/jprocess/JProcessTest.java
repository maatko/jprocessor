package me.mat.jprocess;

import me.mat.jprocess.util.Manifest;
import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.jar.MemoryJar;
import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.mappings.MappingLoadException;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.MappingType;
import me.mat.jprocessor.mappings.generation.GenerationType;
import me.mat.jprocessor.mappings.generation.MappingGenerateException;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.net.URL;

public class JProcessTest {

    private static final String MANIFEST_JSON_URL = "https://launchermeta.mojang.com/v1/packages/86f9645f8398ec902cd17769058851e6fead68cf/1.18.2.json";

    private static final String TEST_JAR_URL = "https://github.com/sim0n/Evaluator/releases/download/1.02/Evaluator-1.0-SNAPSHOT.jar";

    private static final File TESTS_DIRECTORY = new File("tests");
    private static final File MANIFEST_JSON_FILE = new File(TESTS_DIRECTORY, "manifest.json");
    private static final File TEST_JAR_FILE = new File(TESTS_DIRECTORY, "evaluator.jar");
    private static final File TESTS_OUT_JAR_FILE = new File(TESTS_DIRECTORY, "evaluator_out.jar");
    private static final File MAPPINGS_FILE = new File(TESTS_DIRECTORY, "mappings.txt");
    private static final File MAPPINGS_OUT_FILE = new File(TESTS_DIRECTORY, "mappings_out.json");
    private static final File MAPPINGS_TESTS_OUT_FILE = new File(TESTS_DIRECTORY, "tests_mappings_out.json");
    private static final File CLIENT_JAR_FILE = new File(TESTS_DIRECTORY, "client.jar");
    private static final File CLIENT_OUT_JAR_FILE = new File(TESTS_DIRECTORY, "client_out.jar");

    @Test
    public void runMappingTest() throws FileNotFoundException, MappingLoadException {
        // if the test directory does not exist create it
        assert TESTS_DIRECTORY.exists() || TESTS_DIRECTORY.mkdirs();

        // check for the json manifest
        if (!MANIFEST_JSON_FILE.exists()) {
            download(MANIFEST_JSON_URL, MANIFEST_JSON_FILE);
        }

        // load the manifest file
        Manifest manifest = Manifest.load(MANIFEST_JSON_FILE);
        assert manifest != null;

        // if the mappings file does not exist download it
        if (!MAPPINGS_FILE.exists()) {
            download(manifest.downloads.mappings.url, MAPPINGS_FILE);
        }

        // if the client jar file does not exist download it
        if (!CLIENT_JAR_FILE.exists()) {
            download(manifest.downloads.client.url, CLIENT_JAR_FILE);
        }

        // load the mapping file
        MappingManager mappingManager = JProcessor.Mapping.load(MAPPINGS_FILE, MappingType.PROGUARD);

        // load the jar into memory
        MemoryJar memoryJar = JProcessor.Jar.load(CLIENT_JAR_FILE);

        // remap the jar
        memoryJar.remap(mappingManager);

        // save the jar to the disk
        memoryJar.save(CLIENT_OUT_JAR_FILE);

        // save the mappings to a file
        mappingManager.save(MAPPINGS_OUT_FILE);
    }

    @Test
    public void runJarTest() throws FileNotFoundException {
        // if the test directory does not exist create it
        assert TESTS_DIRECTORY.exists() || TESTS_DIRECTORY.mkdirs();

        // check for the jar
        if (!TEST_JAR_FILE.exists()) {
            download(TEST_JAR_URL, TEST_JAR_FILE);
        }

        // load the jar into memory
        MemoryJar memoryJar = JProcessor.Jar.load(TEST_JAR_FILE);

        // check that the jar is loaded
        assert memoryJar.isLoaded();

        // generate the mappings for the current jar
        MappingManager mappingManager;
        try {
            mappingManager = JProcessor.Mapping.generate(GenerationType.ALPHABET, memoryJar);
        } catch (MappingGenerateException e) {
            throw new RuntimeException(e);
        }

        // remap the jar with the generated mappings
        memoryJar.remap(mappingManager);

        // inject a test class into the jar
        MemoryClass cls = memoryJar.createClass(
                Opcodes.V1_8, Opcodes.ACC_PUBLIC, "me/mat/jprocessor/TestClass",
                null, null, null
        );

        // inject a string field into the injected class
        cls.addField(
                Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "testString", "Ljava/lang/String;",
                null, "Hello, World!"
        );

        // inject a method into the injected class
        cls.addMethod(
                Opcodes.ACC_PROTECTED, "testMethod", "()V",
                null, null
        );

        // save the jar to the output file
        memoryJar.save(TESTS_OUT_JAR_FILE);

        // save the mappings
        mappingManager.save(MAPPINGS_TESTS_OUT_FILE);
    }

    /**
     * Downloads a file from the url
     * into the provided file
     *
     * @param url  url that you want to download the file to
     * @param file file object that the file will the written to
     */

    private static void download(String url, File file) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream()); FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
