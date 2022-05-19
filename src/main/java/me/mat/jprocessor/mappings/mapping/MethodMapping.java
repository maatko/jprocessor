package me.mat.jprocessor.mappings.mapping;

import com.google.gson.JsonObject;
import lombok.NonNull;

public class MethodMapping extends FieldMapping {

    public String description;
    public String mappedDescription;

    public MethodMapping(@NonNull String name, @NonNull String mapping, @NonNull String returnType, @NonNull String description) {
        super(name, mapping, returnType);
        this.description = description;
        this.mappedDescription = description;
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
