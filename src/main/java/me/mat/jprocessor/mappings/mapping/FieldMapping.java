package me.mat.jprocessor.mappings.mapping;

import com.google.gson.JsonObject;
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

    @Override
    public JsonObject toJson() {
        JsonObject object = super.toJson();

        object.addProperty("return_type", returnType);
        object.addProperty("mapped_return_type", mappedReturnType);

        return object;
    }

}
