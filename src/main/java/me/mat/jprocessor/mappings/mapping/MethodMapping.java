package me.mat.jprocessor.mappings.mapping;

import lombok.NonNull;

public class MethodMapping extends FieldMapping {

    public String description;
    public String mappedDescription;

    public MethodMapping(@NonNull String name, @NonNull String mapping, @NonNull String returnType, @NonNull String description) {
        super(name, mapping, returnType);
        this.description = description;
        this.mappedDescription = description;
    }

}
