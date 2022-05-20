package me.mat.jprocessor.mappings.mapping;

import com.google.gson.JsonObject;
import lombok.NonNull;

public class FieldMapping extends Mapping {

    @NonNull
    public String returnType;

    @NonNull
    public String mappedReturnType;

    public FieldMapping(@NonNull String name, @NonNull String mapping, @NonNull String returnType, @NonNull String mappedReturnType) {
        super(name, mapping);
        this.returnType = returnType;
        this.mappedReturnType = mappedReturnType;
    }

    public FieldMapping(@NonNull String name, @NonNull String mapping, @NonNull String returnType) {
        this(name, mapping, returnType, returnType);
    }

    /**
     * Converts contents of this class
     * to a json object so it can be saved
     *
     * @return {@link JsonObject}
     */

    @Override
    public JsonObject toJson() {
        JsonObject object = super.toJson();

        object.addProperty("return_type", returnType);
        object.addProperty("mapped_return_type", mappedReturnType);

        return object;
    }

}
