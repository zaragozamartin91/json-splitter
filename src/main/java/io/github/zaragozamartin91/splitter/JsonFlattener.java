package io.github.zaragozamartin91.splitter;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonFlattener {
    private final ObjectMapper mapper = new ObjectMapper();

    boolean keepNulls = true;

    public JsonFlattener discardNulls() {
        this.keepNulls = false;
        return this;
    }

    public Map<String, Object> flatten(String jsonString) throws Exception {
        return flatten(mapper.readTree(jsonString));
    }

    public Map<String, Object> flatten(File file) throws Exception {
        return flatten(mapper.readTree(file));
    }

    private Map<String, Object> flatten(JsonNode root) {
        Map<String, Object> flatMap = new LinkedHashMap<>();
        doFlatten("", root, flatMap);
        return flatMap;
    }

    private void doFlatten(String currentPath, JsonNode node, Map<String, Object> map) {
        if (node == null) return;

        if (node.isObject()) {
            flattenObjectNode(currentPath, (ObjectNode) node, map);
            return;
        } 
        
        if (node.isArray()) {
            flattenArrayNode(currentPath, (ArrayNode) node, map);
            return;
        }
        
        if (node.isNull() && !keepNulls) return;
        
        if (node.isValueNode()) {
            // Map Jackson primitive types to Java types
            if (node.isBoolean()) map.put(currentPath, node.asBoolean());
            else if (node.isLong()) map.put(currentPath, node.asLong());
            else if (node.isInt()) map.put(currentPath, node.asInt());
            else if (node.isDouble()) map.put(currentPath, node.asDouble());
            else map.put(currentPath, node.asText());
        }
    }


    private void flattenObjectNode(String currentPath, ObjectNode node, Map<String, Object> map) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String newPath = currentPath.isEmpty()
                ? entry.getKey()
                : String.format("%s.%s", currentPath, entry.getKey());
            doFlatten(newPath, entry.getValue(), map);
        }
    }

    private void flattenArrayNode(String currentPath, ArrayNode node, Map<String, Object> map) {
        for (int i = 0; i < node.size(); i++) {
            doFlatten(String.format("%s[%d]", currentPath, i), node.get(i), map);
        }
    }
}
