package io.github.zaragozamartin91.splitter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSplitter {
    private ObjectMapper objectMapper;

    public JsonSplitter() {
        this(new ObjectMapper());
    }

    public JsonSplitter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SplitJson split(JsonSource source, SplitStrategy strategy) {
        JsonUnit jsonUnit = new JsonUnit(source, objectMapper);
        return jsonUnit.split(strategy);
    }
}
