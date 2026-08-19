package io.github.zaragozamartin91.splitter;

import java.util.List;

/**
 * Container for the resulting set of JSON parts.
 */
public class SplitJson {
    private final List<JsonPart> jsonParts;

    SplitJson(List<JsonPart> jsonParts) {
        this.jsonParts = jsonParts;
    }

    /**
     * Returns the collection of split JSON parts.
     * @return The list of JSON parts
     */
    public List<JsonPart> getParts() {
        return jsonParts;
    }
}
