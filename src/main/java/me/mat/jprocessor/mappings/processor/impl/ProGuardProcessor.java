package me.mat.jprocessor.mappings.processor.impl;

import me.mat.jprocessor.mappings.MappingManager;
import me.mat.jprocessor.mappings.processor.MappingProcessor;

public class ProGuardProcessor implements MappingProcessor {

    private static final String TAB = "    ";
    private static final String DELIMITER = " -> ";

    private MappingManager mappingManager;

    @Override
    public void process(String line) {
        if (line.startsWith("#")) {
            return;
        }
        if (!line.startsWith(TAB)) {
            String[] data = line.split(DELIMITER);
            mappingManager.mapClass(data[0], data[1].substring(0, data[1].length() - 1));
        } else {
            line = line.substring(TAB.length());
            if (line.contains(":")) {
                line = line.substring(line.lastIndexOf(":") + 1);
            }
            String[] data = line.split(DELIMITER);
            String[] subData = data[0].split(" ");
            if (!line.contains("(")) {
                mappingManager.mapField(subData[1], data[1], subData[0]);
            } else {
                mappingManager.mapMethod(
                        subData[1].substring(0, subData[1].indexOf("(")),
                        data[1],
                        subData[0],
                        subData[1].substring(subData[1].indexOf("(") + 1, subData[1].indexOf(")"))
                );
            }
        }
    }

    @Override
    public void manager(MappingManager mappingManager) {
        this.mappingManager = mappingManager;
    }

}
