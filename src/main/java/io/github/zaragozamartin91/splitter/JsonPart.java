package io.github.zaragozamartin91.splitter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@PublicApi
public class JsonPart {
    private final Map<String, Object> jsonMap;
    private final List<Object> jsonArray;

    JsonPart(Map<String, Object> jsonMap, List<Object> jsonArray) {
        this.jsonMap = jsonMap;
        this.jsonArray = jsonArray;
    }

    static JsonPart map(Map<String, Object> jsonMap) {
        return new JsonPart(jsonMap, null);
    }

    static JsonPart array(List<Object> jsonArray) {
        return new JsonPart(null, jsonArray);
    }

    public Map<String, Object> jsonMap() {
        return jsonMap;
    }

    public List<Object> jsonArray() {
        return jsonArray;
    }

    public JsonRootType rootType() {
        if (Objects.nonNull(jsonMap)) return JsonRootType.JSON_MAP;
        if (Objects.nonNull(jsonArray)) return JsonRootType.JSON_ARRAY;
        return JsonRootType.NULL;
    }
}
