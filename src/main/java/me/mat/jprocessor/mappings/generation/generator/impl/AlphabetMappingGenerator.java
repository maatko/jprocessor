package me.mat.jprocessor.mappings.generation.generator.impl;

import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.jar.cls.MemoryField;
import me.mat.jprocessor.mappings.generation.generator.MappingGenerator;

public class AlphabetMappingGenerator extends MappingGenerator {

    private final NameGenerator classNameGenerator = new NameGenerator();

    private NameGenerator fieldNameGenerator;

    @Override
    public String mapClass(String className, MemoryClass memoryClass) {
        this.fieldNameGenerator = new NameGenerator();
        return classNameGenerator.generate();
    }

    @Override
    public String mapField(String className, MemoryClass memoryClass, MemoryField memoryField) {
        return fieldNameGenerator.generate();
    }

    private static final class NameGenerator {

        private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
        private static final int ALPHABET_LENGTH = ALPHABET.length() - 1;

        private String currentName = null;

        public String generate() {
            if (currentName == null) {
                currentName = String.valueOf(ALPHABET.charAt(0));
                return currentName;
            }

            char lastChar = getLastChar();
            boolean upperCase = Character.isUpperCase(lastChar);
            lastChar = Character.toLowerCase(lastChar);
            int index = ALPHABET.indexOf(lastChar);

            if (index == ALPHABET_LENGTH) {
                if (!upperCase) {
                    index = -1;
                    upperCase = true;
                } else {
                    currentName += ALPHABET.charAt(0);
                    return currentName;
                }
            }

            // clip the last character off the name
            currentName = currentName.substring(0, currentName.length() - 1);

            // append the next character
            char nextChar = ALPHABET.charAt(index + 1);
            if (upperCase) {
                nextChar = Character.toUpperCase(nextChar);
            }
            currentName += nextChar;

            // return the current name
            return currentName;
        }

        private char getLastChar() {
            return currentName.charAt(currentName.length() - 1);
        }

    }

}
