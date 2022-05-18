package me.mat.jprocessor.mappings.mapping;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Mapping {

    @NonNull
    public String name;

    @NonNull
    public String mapping;

    public Mapping(@NonNull String name) {
        this(name, name);
    }

}
