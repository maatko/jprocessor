package me.mat.jprocessor.mappings;

public interface MappingLoadCallback {

    void onLoad(MappingManager mappingManager);

    void onFail(String reason);

}
