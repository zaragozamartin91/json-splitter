package io.github.zaragozamartin91.splitter;

import java.util.List;

public class SplitJson {
    private final List<JsonPart> jsonParts;

    public SplitJson(List<JsonPart> jsonParts) {
        this.jsonParts = jsonParts;
    }

    public List<JsonPart> getParts() {
        return jsonParts;
    }
}
