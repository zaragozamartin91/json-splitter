package io.github.zaragozamartin91.splitter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@PublicApi
public class JsonSource {
    /* Only one of these can be nonNull at a time */
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


    String getTextJson() {
        return textJson;
    }

    Map<String, Object> getDictionaryJson() {
        return dictionaryJson;
    }

    enum ContentType {
        TEXT, DICTIONARY, FLAT_JSON, NONE
    }

    ContentType getContentType() {
        if (Objects.nonNull(textJson)) return ContentType.TEXT;
        if (Objects.nonNull(dictionaryJson)) return ContentType.DICTIONARY;
        return ContentType.NONE;
    }

    Object getContent() {
        Object content = null;
        switch (getContentType()) {
            case TEXT: content=getTextJson(); break;
            case DICTIONARY: content=getDictionaryJson(); break;
            case NONE: break;
        }
        return content;
    }

    @Override
    public String toString() {
        return "JsonSource [textJson=" + textJson + ", dictionaryJson=" + dictionaryJson + "]";
    }
}
