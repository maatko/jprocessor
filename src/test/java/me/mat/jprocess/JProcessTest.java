package me.mat.jprocess;

import me.mat.jprocessor.memory.MemoryJar;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class JProcessTest {

    private static final String TEST_JAR_URL = "https://github.com/sim0n/Evaluator/releases/download/1.02/Evaluator-1.0-SNAPSHOT.jar";

    private static final File EVALUATOR = new File("tests", "evaluator.jar");

    @Test
    public void test() {
        MemoryJar memoryJar = new MemoryJar(getEvaluator());
    }

    private static File getEvaluator() {
        if (!EVALUATOR.getParentFile().exists())
            if (!EVALUATOR.getParentFile().mkdirs())
                throw new RuntimeException("Failed to create the tests directory");

        if (!EVALUATOR.exists()) {
            download(TEST_JAR_URL, EVALUATOR);
        }
        return EVALUATOR;
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
