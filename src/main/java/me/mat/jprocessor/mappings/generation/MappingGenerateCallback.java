package me.mat.jprocessor.mappings.generation;

import me.mat.jprocessor.mappings.MappingManager;

public interface MappingGenerateCallback {

    void onFinish(MappingManager mappingManager);

    void onFail(String reason);

}
