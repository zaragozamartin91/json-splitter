package io.github.zaragozamartin91.splitter;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@PublicApi
public class JsonFlattener {
    private final JsonCodec jsonCodec;

    boolean keepNulls = true;

    JsonFlattener() {
        this(JsonCodec.instance());
    }

    JsonFlattener(JsonCodec jsonCodec) {
        this.jsonCodec = jsonCodec;
    }

    public JsonFlattener discardNulls() {
        this.keepNulls = false;
        return this;
    }

    public FlatJson flatten(Object data) {
        JsonCodec.JsonBox jsonBox = jsonCodec.polyReadTree(data);
        return new FlatJson(flatten(jsonBox));
    }

    public FlatJson flatten(String jsonString) {
        return new FlatJson(flatten(jsonCodec.readTree(jsonString)));
    }

    public FlatJson flatten(File file) {
        return new FlatJson(flatten(jsonCodec.readTree(file)));
    }

    public FlatJson flatten(Map<String, Object> map) {
        JsonCodec.JsonBox jsonNode = jsonCodec.valueToTree(map);
        return new FlatJson(flatten(jsonNode));
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
