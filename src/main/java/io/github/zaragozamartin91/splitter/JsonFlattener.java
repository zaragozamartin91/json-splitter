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
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String newPath = currentPath.isEmpty() 
                    ? entry.getKey() 
                    : currentPath + "." + entry.getKey();
                doFlatten(newPath, entry.getValue(), map);
            }
            return;
        } 
        
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                doFlatten(currentPath + "[" + i + "]", arrayNode.get(i), map);
            }
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
}
