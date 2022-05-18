package me.mat.jprocessor.mappings.mapping;

import lombok.NonNull;

public class FieldMapping extends Mapping {

    @NonNull
    public String returnType;

    @NonNull
    public String mappedReturnType;

    public FieldMapping(@NonNull String name, @NonNull String mapping, @NonNull String returnType) {
        super(name, mapping);
        this.returnType = returnType;
        this.mappedReturnType = returnType;
    }

}
