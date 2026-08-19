package io.github.zaragozamartin91.splitter;

import java.util.List;

@PublicApi
public class SplitJson {
    private final List<JsonPart> jsonParts;

    SplitJson(List<JsonPart> jsonParts) {
        this.jsonParts = jsonParts;
    }

    public List<JsonPart> getParts() {
        return jsonParts;
    }
}
