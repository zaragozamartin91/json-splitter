package io.github.zaragozamartin91.splitter;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Logic for converting nested JSON into a flat map using dot-notation keys.
 */
public class JsonFlattener {
    private final JsonCodec jsonCodec;

    boolean keepNulls = true;

    JsonFlattener() {
        this(JsonCodec.instance());
    }

    JsonFlattener(JsonCodec jsonCodec) {
        this.jsonCodec = jsonCodec;
    }

    /**
     * Configures the flattener to exclude null values from the resulting flat map.
     * @return This flattener instance for method chaining
     */
    public JsonFlattener discardNulls() {
        this.keepNulls = false;
        return this;
    }

    /**
     * Flattens a Java object into a JSON part containing a flat map.
     * @param data The object to flatten
     * @return A JsonPart containing the flattened map
     */
    public JsonPart flatten(Object data) {
        JsonCodec.JsonBox jsonBox = jsonCodec.polyReadTree(data);
        return JsonPart.map(flatten(jsonBox));
    }

    /**
     * Flattens a JSON string into a JSON part containing a flat map.
     * @param jsonString The JSON string to flatten
     * @return A JsonPart containing the flattened map
     */
    public JsonPart flatten(String jsonString) {
        return JsonPart.map(flatten(jsonCodec.readTree(jsonString)));
    }

    /**
     * Flattens a JSON file into a JSON part containing a flat map.
     * @param file The JSON file to flatten
     * @return A JsonPart containing the flattened map
     */
    public JsonPart flatten(File file) {
        return JsonPart.map(flatten(jsonCodec.readTree(file)));
    }

    /**
     * Flattens a JSON map into a JSON part containing a flat map.
     * @param map The map to flatten
     * @return A JsonPart containing the flattened map
     */
    public JsonPart flatten(Map<String, Object> map) {
        JsonCodec.JsonBox jsonNode = jsonCodec.valueToTree(map);
        return JsonPart.map(flatten(jsonNode));
    }

    private Map<String, Object> flatten(JsonCodec.JsonBox root) {
        Map<String, Object> flatMap = new LinkedHashMap<>();
        doFlatten("", root, flatMap);
        return flatMap;
    }

    private void doFlatten(String currentPath, JsonCodec.JsonBox node, Map<String, Object> accumulator) {
        if (node == null) return;

        if (node.isObject()) {
            flattenObjectNode(currentPath, node, accumulator);
            return;
        }

        if (node.isArray()) {
            flattenArrayNode(currentPath, node, accumulator);
            return;
        }

        if (node.isNull() && discardNullValues()) return;

        if (node.isValueNode()) {
            // Map Jackson primitive types to Java types
            if (node.isBoolean()) accumulator.put(currentPath, node.asBoolean());
            else if (node.isLong()) accumulator.put(currentPath, node.asLong());
            else if (node.isInt()) accumulator.put(currentPath, node.asInt());
            else if (node.isDouble()) accumulator.put(currentPath, node.asDouble());
            else accumulator.put(currentPath, node.asText());
        }
    }

    private boolean discardNullValues() {
        return !keepNulls;
    }

    private void flattenObjectNode(String currentPath, JsonCodec.JsonBox node, Map<String, Object> accumulator) {
        for (Iterator<Map.Entry<String, JsonCodec.JsonBox>> it = node.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonCodec.JsonBox> entry = it.next();
            String newPath = currentPath.isEmpty()
                    ? entry.getKey()
                    : String.format("%s.%s", currentPath, entry.getKey());
            doFlatten(newPath, entry.getValue(), accumulator);
        }
    }

    private void flattenArrayNode(String currentPath, JsonCodec.JsonBox node, Map<String, Object> accumulator) {
        for (int i = 0; i < node.size(); i++) {
            doFlatten(String.format("%s[%d]", currentPath, i), node.get(i), accumulator);
        }
    }
}
