package me.mat.jprocessor.mappings.generation.generator.impl;

import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.jar.cls.MemoryField;
import me.mat.jprocessor.jar.cls.MemoryMethod;
import me.mat.jprocessor.mappings.generation.generator.MappingGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomMappingGenerator extends MappingGenerator {

    private final RandomNameGenerator classNameGenerator = new RandomNameGenerator();

    private RandomNameGenerator fieldNameGenerator;
    private RandomNameGenerator methodNameGenerator;

    @Override
    public String mapClass(String className, MemoryClass memoryClass) {
        fieldNameGenerator = new RandomNameGenerator();
        methodNameGenerator = new RandomNameGenerator();
        return classNameGenerator.generate();
    }

    @Override
    public String mapField(String className, MemoryClass memoryClass, MemoryField memoryField) {
        return fieldNameGenerator.generate();
    }

    @Override
    public String mapMethod(String className, MemoryClass memoryClass, MemoryMethod memoryMethod) {
        return methodNameGenerator.generate();
    }

    public static final class RandomNameGenerator {

        private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private static final Random RANDOM = new Random();
        private static final int LENGTH = 5;

        private final List<String> generated = new ArrayList<>();

        public String generate() {
            String random;
            do {
                random = random();
            } while (generated.contains(random));
            generated.add(random);
            return random;
        }

        public String random() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < LENGTH; i++) {
                builder.append(ALPHABET.charAt(Math.abs(RANDOM.nextInt(ALPHABET.length() - 1))));
            }
            return builder.toString();
        }

    }

}
