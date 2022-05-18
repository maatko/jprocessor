package me.mat.jprocess;

import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.jar.MemoryJar;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URL;

public class JProcessTest {

    private static final String TEST_JAR_URL = "https://github.com/sim0n/Evaluator/releases/download/1.02/Evaluator-1.0-SNAPSHOT.jar";

    private static final File TEST_JAR_FILE = new File("test.jar");

    @Test
    public void runJarTest() throws FileNotFoundException {
        // check for the jar
        checkJar();

        // load the jar into memory
        MemoryJar memoryJar = JProcessor.load(TEST_JAR_FILE);

        // check that the classes were loaded
        assert !memoryJar.loadedClasses.isEmpty();

        // check that the resources were loaded
        assert !memoryJar.loadedResources.isEmpty();
    }

    /**
     * Checks that the test jar is there
     * if it's not there it downloads the jar
     */

    public static void checkJar() {
        if (!TEST_JAR_FILE.exists()) {
            try (BufferedInputStream in = new BufferedInputStream(new URL(TEST_JAR_URL).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(TEST_JAR_FILE.getAbsolutePath())) {
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

}
