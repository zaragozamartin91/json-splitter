package io.github.zaragozamartin91.splitter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class JsonSource {
    private String textJson;
    private Map<String, Object> dictionaryJson;

    public static JsonSource fromString(String jsonData) {
        return new JsonSource(Objects.requireNonNull(jsonData, "jsonData must not be null"));
    }

    public static JsonSource fromBytes(byte[] jsonData, Charset charset) {
        return new JsonSource(new String(
                Objects.requireNonNull(jsonData, "jsonData must not be null"),
                Objects.requireNonNull(charset, "charset must not be null")));
    }

    public static JsonSource fromMap(Map<String, Object> mapJson) {
        return new JsonSource(Objects.requireNonNull(mapJson, "mapJson must not be null"));
    }

    public static JsonSource fromPath(Path path, Charset charset) {
        try {
            byte[] fileBytes = Files.readAllBytes(path);
            return fromBytes(fileBytes, charset);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read data from path: " + path, e);
        }
    }

    JsonSource(String jsonData) {
        this.textJson = jsonData;
    }

    JsonSource(Map<String, Object> mapJson) {
        this.dictionaryJson = mapJson;
    }

    public String getTextJson() {
        return textJson;
    }

    public Map<String, Object> getDictionaryJson() {
        return dictionaryJson;
    }

    static enum ContentType {
        TEXT, DICTIONARY, NONE
    }

    public ContentType getContentType() {
        if (Objects.nonNull(textJson)) {
            return ContentType.TEXT;
        } else if (Objects.nonNull(dictionaryJson)) {
            return ContentType.DICTIONARY;
        } else {
            return ContentType.NONE;
        }
    }
}
