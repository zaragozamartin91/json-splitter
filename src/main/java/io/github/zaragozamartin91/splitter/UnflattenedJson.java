package io.github.zaragozamartin91.splitter;

import java.util.Map;

public class UnflattenedJson {
    private final Map<String, Object> jsonMap;

    public UnflattenedJson(Map<String, Object> jsonMap) {
        this.jsonMap = jsonMap;
    }

    public Map<String, Object> getJsonMap() {
        return jsonMap;
    }
}
