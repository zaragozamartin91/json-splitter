package io.github.zaragozamartin91.splitter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class JsonSourceNeo {
    /* Only one of these can be nonNull at a time */
    private String textJson;
    private Map<String, Object> dictionaryJson;
    private FlatJson flatJson;

    public static JsonSourceNeo fromString(String jsonData) {
        return new JsonSourceNeo(Objects.requireNonNull(jsonData, "jsonData must not be null"));
    }

    public static JsonSourceNeo fromBytes(byte[] jsonData, Charset charset) {
        return new JsonSourceNeo(new String(
                Objects.requireNonNull(jsonData, "jsonData must not be null"),
                Objects.requireNonNull(charset, "charset must not be null")));
    }

    public static JsonSourceNeo fromMap(Map<String, Object> mapJson) {
        return new JsonSourceNeo(Objects.requireNonNull(mapJson, "mapJson must not be null"));
    }

    public static JsonSourceNeo fromPath(Path path, Charset charset) {
        try {
            byte[] fileBytes = Files.readAllBytes(path);
            return fromBytes(fileBytes, charset);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read data from path: " + path, e);
        }
    }

    public static JsonSourceNeo fromFlatJson(FlatJson flatJson) {
        return new JsonSourceNeo(flatJson);
    }

    JsonSourceNeo(String jsonData) {
        this.textJson = jsonData;
    }

    JsonSourceNeo(Map<String, Object> mapJson) {
        this.dictionaryJson = mapJson;
    }

    JsonSourceNeo(FlatJson flatJson) {
        this.flatJson = flatJson;
    }

    String getTextJson() {
        return textJson;
    }

    Map<String, Object> getDictionaryJson() {
        return dictionaryJson;
    }

    FlatJson getFlatJson() {
        return flatJson;
    }

    enum ContentType {
        TEXT, DICTIONARY, FLAT_JSON, NONE
    }

    ContentType getContentType() {
        if (Objects.nonNull(textJson)) return ContentType.TEXT;
        if (Objects.nonNull(dictionaryJson)) return ContentType.DICTIONARY;
        if (Objects.nonNull(flatJson)) return ContentType.FLAT_JSON;
        return ContentType.NONE;
    }

    Object getContent() {
        Object content = null;
        switch (getContentType()) {
            case TEXT: content=getTextJson(); break;
            case DICTIONARY: content=getDictionaryJson(); break;
            case FLAT_JSON: content=getFlatJson(); break;
            case NONE: break;
        }
        return content;
    }

    @Override
    public String toString() {
        return "JsonSource [textJson=" + textJson + ", dictionaryJson=" + dictionaryJson + "]";
    }
}
