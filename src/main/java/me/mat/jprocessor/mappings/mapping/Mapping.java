package me.mat.jprocessor.mappings.mapping;

import com.google.gson.JsonObject;
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

    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("name", name);
        object.addProperty("mapping", mapping);

        return object;
    }

}
