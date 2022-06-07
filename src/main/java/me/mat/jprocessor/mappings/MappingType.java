package me.mat.jprocessor.mappings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mat.jprocessor.mappings.mapping.processor.MappingProcessor;
import me.mat.jprocessor.mappings.mapping.processor.impl.ProGuardProcessor;
import me.mat.jprocessor.mappings.mapping.processor.impl.SRGProcessor;

@Getter
@RequiredArgsConstructor
public enum MappingType {

    PROGUARD(new ProGuardProcessor()),
    SRG(new SRGProcessor());

    @NonNull
    private final MappingProcessor processor;

}
