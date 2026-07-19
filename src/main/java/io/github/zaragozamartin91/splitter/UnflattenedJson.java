package io.github.zaragozamartin91.splitter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UnflattenedJson {
    private final Map<String, Object> jsonMap;
    private final List<Object> jsonArray;

    UnflattenedJson(Map<String, Object> jsonMap) {
        this.jsonMap = jsonMap;
        jsonArray = null;
    }

    UnflattenedJson(List<Object> jsonArray) {
        jsonMap = null;
        this.jsonArray = jsonArray;
    }

    public Map<String, Object> getJsonMap() {
        return jsonMap;
    }

    public List<Object> getJsonArray() {
        return jsonArray;
    }

    public ResultType resultType() {
        if (Objects.nonNull(jsonMap)) return ResultType.JSON_MAP;

        if (Objects.nonNull(jsonArray)) return ResultType.JSON_ARRAY;

        return ResultType.NULL;
    }

    public enum ResultType {
        JSON_MAP,
        JSON_ARRAY,
        NULL
    }
}
