package me.mat.jprocessor.mappings.generation.generator.impl;

import me.mat.jprocessor.jar.MemoryJar;
import me.mat.jprocessor.jar.cls.MemoryClass;
import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.generation.generator.MappingGenerator;
import me.mat.jprocessor.mappings.mapping.Mapping;
import org.objectweb.asm.tree.InnerClassNode;

public class AlphabetMappingGenerator implements MappingGenerator {

    private MappingManager mappingManager;

    private NameGenerator classNameGenerator;
    private NameGenerator fieldNameGenerator;
    private NameGenerator methodNameGenerator;

    @Override
    public void generate(String className, MemoryJar memoryJar, MemoryClass memoryClass) {
        // reset the generator
        if (reset(memoryClass) || memoryClass.isMainClass) {
            return;
        }

        // clear all the inner class nodes from the class
        memoryClass.classNode.innerClasses.clear();

        // generate a mapping for the current class
        mappingManager.mapClass(className, classNameGenerator.generate());

        // make sure that the class is not an enum
        if (!memoryClass.isEnum()) {

            // generate mappings for all the fields in the current class
            memoryClass.fields.forEach(memoryField -> mappingManager.mapField(
                    memoryField.fieldNode.name,
                    fieldNameGenerator.generate(),
                    memoryField.fieldNode.desc
            ));
        }
    }

    @Override
    public void generateInner(String className, MemoryJar memoryJar, MemoryClass memoryClass) {
        // reset the generator
        if (!reset(memoryClass) || memoryClass.isMainClass) {
            return;
        }

        // if the class does not have an outer class
        if (memoryClass.outerClass == null) {
            // return out of the method
            return;
        }

        // get the mapping
        Mapping mapping = mappingManager.getClass(memoryClass.classNode.outerClass);

        // get the outer name
        String outerName = mapping != null ? mapping.mapping : memoryClass.classNode.outerClass;

        // update the outer name of the class node
        memoryClass.classNode.outerClass = outerName;

        // generate the mapping
        String innerClassMapping = outerName + "$" + classNameGenerator.generate();

        // generate a mapping for the current class
        mappingManager.mapClass(className, innerClassMapping);

        // add the inner class
        memoryClass.outerClass.classNode.innerClasses.add(new InnerClassNode(
                innerClassMapping,
                null,
                null,
                0
        ));
    }

    @Override
    public void manager(MappingManager mappingManager) {
        this.mappingManager = mappingManager;
        this.classNameGenerator = new NameGenerator();
    }

    boolean reset(MemoryClass memoryClass) {
        this.fieldNameGenerator = new NameGenerator();
        this.methodNameGenerator = new NameGenerator();

        return memoryClass.isInnerClass;
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
