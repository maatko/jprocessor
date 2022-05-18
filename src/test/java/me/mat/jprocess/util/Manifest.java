package me.mat.jprocess.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@AllArgsConstructor
public final class Manifest {

    private static final Gson GSON = new GsonBuilder().serializeNulls().serializeNulls().create();

    @SerializedName("downloads")
    public final Downloads downloads;

    @AllArgsConstructor
    public static final class Downloads {

        @SerializedName("client")
        public final Item client;

        @SerializedName("client_mappings")
        public final Item mappings;

        @AllArgsConstructor
        public static final class Item {

            @SerializedName("size")
            public final int size;

            @SerializedName("url")
            public final String url;

        }

    }

    /**
     * Loads the manifest from a file
     *
     * @param file file that you want to load from
     * @return {@link Manifest}
     */

    public static Manifest load(File file) {
        try (FileReader fileReader = new FileReader(file)) {
            return GSON.fromJson(fileReader, Manifest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
