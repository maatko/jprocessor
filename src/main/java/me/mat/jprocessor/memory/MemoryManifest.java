package me.mat.jprocessor.memory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.jar.Manifest;

@Getter
@RequiredArgsConstructor
public class MemoryManifest {

    private final Manifest manifest;

}
