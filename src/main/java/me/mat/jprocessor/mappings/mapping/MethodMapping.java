package me.mat.jprocessor.mappings.mapping;

import com.google.gson.JsonObject;
import lombok.NonNull;

public class MethodMapping extends FieldMapping {

    public String description;
    public String mappedDescription;

    public MethodMapping(@NonNull String name, @NonNull String mapping, @NonNull String returnType, @NonNull String mappedReturnType, @NonNull String description, @NonNull String mappedDescription) {
        super(name, mapping, returnType, mappedReturnType);
        this.description = description;
        this.mappedDescription = mappedDescription;
    }

    public MethodMapping(@NonNull String name, @NonNull String mapping, @NonNull String returnType, @NonNull String description) {
        this(name, mapping, returnType, returnType, description, description);
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

        object.addProperty("description", description);
        object.addProperty("mapped_description", mappedDescription);

        return object;
    }

}
