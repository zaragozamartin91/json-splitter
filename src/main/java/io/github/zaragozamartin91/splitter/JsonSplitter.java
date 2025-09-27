package io.github.zaragozamartin91.splitter;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.base.JacksonJsonValue;
import com.github.wnameless.json.base.JsonValueBase;
import com.github.wnameless.json.flattener.JsonFlattener;

import io.github.zaragozamartin91.splitter.JsonSource.ContentType;

public class JsonSplitter {
    private JsonSource source;
    private ObjectMapper objectMapper;

    public JsonSplitter(JsonSource source) {
        this(source, new ObjectMapper());
    }

    public JsonSplitter(JsonSource source, ObjectMapper objectMapper) {
        this.source = source;
        this.objectMapper = objectMapper;
    }

    public JsonOutput split(SplitStrategy strategy) {
        /*
         * Steps:
         * 1. Read JSON data from the source
         * 2. Parse the JSON data into something that can be fed to the json-flatten
         * library
         * 3. Use the json-flatten library to flatten the JSON data
         * 4. Split the flattened data based on the strategy
         * 5. Return the split data as a JsonOutput object
         */

        ContentType contentType = source.getContentType();
        Map<String, Object> flatJsonDictionary;
        switch (contentType) {
            case TEXT:
                flatJsonDictionary = JsonFlattener.flattenAsMap(source.getTextJson());
                break;
            case DICTIONARY:
                JsonNode jsonNode = objectMapper.valueToTree(source.getDictionaryJson());
                JacksonJsonValue jsonVal = new JacksonJsonValue(jsonNode);
                flatJsonDictionary = JsonFlattener.flattenAsMap(jsonVal);
                break;
            default:
                throw new IllegalStateException("JsonSource has no content");
        }

        FlatJson flatJson = new FlatJson(flatJsonDictionary);
        SplitJson splitJson = flatJson.split(strategy);

        // Implement splitting logic based on the strategy
        return new JsonOutput(splitJson);
    }
}
