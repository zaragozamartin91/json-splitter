package io.github.zaragozamartin91.splitter;

import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.base.JacksonJsonValue;
import com.github.wnameless.json.flattener.JsonFlattener;

import io.github.zaragozamartin91.splitter.JsonSource.ContentType;

public class JsonUnit {
    private JsonSource source;
    private ObjectMapper objectMapper;

    public JsonUnit(JsonSource source, ObjectMapper objectMapper) {
        this.source = source;
        this.objectMapper = objectMapper;
    }

    public FlatJson flatten() {
        ContentType contentType = source.getContentType();
        switch (contentType) {
            case TEXT:
                String textJson = source.getTextJson();
                return new FlatJson(JsonFlattener.flattenAsMap(textJson));
            case DICTIONARY:
                Map<String, Object> dictionaryJson = source.getDictionaryJson();
                JsonNode jsonNode = objectMapper.valueToTree(dictionaryJson);
                JacksonJsonValue jsonVal = new JacksonJsonValue(jsonNode);
                return new FlatJson(JsonFlattener.flattenAsMap(jsonVal));
            case FLAT_JSON:
                return source.getFlatJson();
            default:
                throw new IllegalStateException("JsonSource has no content");
        }
    }

    public FlatJson plain() {
        ContentType contentType = source.getContentType();
        Map<String, Object> flatJsonDictionary;
        switch (contentType) {
            case TEXT:
                try {
                    flatJsonDictionary = objectMapper.readValue(source.getTextJson(),
                            new TypeReference<Map<String, Object>>() {
                            });
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                break;
            case DICTIONARY:
                flatJsonDictionary = source.getDictionaryJson();
                break;
            case FLAT_JSON:
                return source.getFlatJson();
            default:
                throw new IllegalStateException("JsonSource has no content");
        }

        return new FlatJson(flatJsonDictionary);
    }

    public SplitJson split(SplitStrategy strategy) {
        return strategy.split(this);
    }
}
