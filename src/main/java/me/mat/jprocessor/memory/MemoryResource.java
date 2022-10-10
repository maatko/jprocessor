package me.mat.jprocessor.memory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemoryResource {

    @NonNull
    private byte[] data;

}
