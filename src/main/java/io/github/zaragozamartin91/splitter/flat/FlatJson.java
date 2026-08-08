package io.github.zaragozamartin91.splitter.flat;

import java.util.List;
import java.util.Map;

public class FlatJson {
    private final Map<String, Object> jsonMap;

    public FlatJson(Map<String, Object> jsonMap) {
        this.jsonMap = jsonMap;
    }

    public Map<String, Object> jsonMap() {
        return jsonMap;
    }
}
