package io.github.zaragozamartin91.splitter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data holder for a single fragment of the split JSON.
 */
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

    /**
     * Returns the JSON map content if the part is a map.
     * @return The JSON map, or null if the part is not a map
     */
    public Map<String, Object> jsonMap() {
        return jsonMap;
    }

    /**
     * Returns the JSON array content if the part is an array.
     * @return The JSON array, or null if the part is not an array
     */
    public List<Object> jsonArray() {
        return jsonArray;
    }

    /**
     * Returns the root structural type of this JSON part.
     * @return The root type of the JSON part
     */
    public JsonRootType rootType() {
        if (Objects.nonNull(jsonMap)) return JsonRootType.JSON_MAP;
        if (Objects.nonNull(jsonArray)) return JsonRootType.JSON_ARRAY;
        return JsonRootType.NULL;
    }

    Object content() {
        Object content;
        switch (this.rootType()) {
            case JSON_MAP: content = this.jsonMap(); break;
            case JSON_ARRAY: content = this.jsonArray(); break;
            default: content = null;
        }
        return content;
    }
}
