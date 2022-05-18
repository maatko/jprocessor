package me.mat.jprocess;

import me.mat.jprocessor.JProcessor;
import me.mat.jprocessor.jar.MemoryJar;

import java.awt.*;
import java.io.FileNotFoundException;

public class JProcessTest {

    private final MemoryJar memoryJar;

    private JProcessTest() {
        try {
            this.memoryJar = JProcessor.load(System.getenv("APPDATA") + "/.minecraft/versions/1.8.9/1.8.9.jar");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(JProcessTest::new);
    }

}
