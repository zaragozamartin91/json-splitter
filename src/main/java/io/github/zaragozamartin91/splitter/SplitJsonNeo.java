package io.github.zaragozamartin91.splitter;

import java.util.List;

public class SplitJsonNeo {
    private final List<JsonPart> jsonParts;

    public SplitJsonNeo(List<JsonPart> jsonParts) {
        this.jsonParts = jsonParts;
    }

    public List<JsonPart> getParts() {
        return jsonParts;
    }
}
