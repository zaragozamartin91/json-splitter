package io.github.zaragozamartin91.splitter;

import java.util.List;
import java.util.Map;

public class JsonPart {
    private final Map<String, Object> jsonMap;
    private final List<Object> jsonArray;

    public JsonPart(Map<String, Object> jsonMap, List<Object> jsonArray) {
        this.jsonMap = jsonMap;
        this.jsonArray = jsonArray;
    }

    public static JsonPart map(Map<String, Object> jsonMap) {
        return new JsonPart(jsonMap, null);
    }

    public static JsonPart array(List<Object> jsonArray) {
        return new JsonPart(null, jsonArray);
    }

    public Map<String, Object> jsonMap() {
        return jsonMap;
    }

    public List<Object> jsonArray() {
        return jsonArray;
    }
}
