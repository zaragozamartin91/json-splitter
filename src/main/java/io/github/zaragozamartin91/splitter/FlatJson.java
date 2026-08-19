package io.github.zaragozamartin91.splitter;

import java.util.Map;

@PublicApi
public class FlatJson {
    private final Map<String, Object> jsonMap;

    FlatJson(Map<String, Object> jsonMap) {
        this.jsonMap = jsonMap;
    }

    public Map<String, Object> jsonMap() {
        return jsonMap;
    }
}
