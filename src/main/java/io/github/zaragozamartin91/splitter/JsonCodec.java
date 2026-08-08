package io.github.zaragozamartin91.splitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

public class JsonCodec {
    public static JsonCodec instance() { return new JsonCodec(); }

    private final ObjectMapper mapper = new ObjectMapper();

    public JsonBox readTree(String jsonString) {
        try {
            return new JsonBox(mapper.readTree(jsonString));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonBox readTree(File file) {
        try {
            return new JsonBox(mapper.readTree(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonBox valueToTree(Map<?, ?> map) {
        return new JsonBox(mapper.valueToTree(map));
    }

    public JsonBox polyReadTree(Object input) {
        if (input instanceof String) return readTree((String) input);
        if (input instanceof File) return readTree((File) input);
        if (input instanceof Map) return valueToTree((Map<?, ?>) input);

        throw new IllegalArgumentException(
            "Unsupported input type: " + (input == null ? "null" : input.getClass().getName())
        );
    }

    public Map<String, Object> polyReadMap(Object input) {
        JsonBox box = polyReadTree(input);
        if (!box.isObject()) throw new IllegalArgumentException("Wrapped node is not an object");
        return toMap(box);
    }

    public Map<String, Object> toMap(JsonBox box) {
        return mapper.convertValue(box.node, Map.class);
    }

    public byte[] writeValueAsBytes(Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int sizeInBytes(Object value) {
        return writeValueAsBytes(value).length;
    }

    public static class JsonBox {
        JsonNode node;

        public JsonBox(JsonNode node) {
            this.node = node;
        }

        public boolean isObject() { return node.isObject(); }
        public boolean isArray() { return node.isArray(); }
        public boolean isNull() { return node.isNull(); }
        public boolean isValueNode() { return node.isValueNode(); }
        public boolean isBoolean() { return node.isBoolean(); }
        public boolean asBoolean() { return node.asBoolean(); }
        public boolean isLong() { return node.isLong(); }
        public long asLong() { return node.asLong(); }
        public boolean isInt() { return node.isInt(); }
        public int asInt() { return node.asInt(); }
        public boolean isDouble() { return node.isDouble(); }
        public double asDouble() { return node.asDouble(); }
        public String asText() { return node.asText(); }

        public JsonBox get(int index) {
            if (!isArray()) { throw new IllegalArgumentException("Wrapped node is not an array"); }
            return new JsonBox(node.get(index));
        }

        public int size() {
            return node.size();
        }

        public Iterator<Map.Entry<String, JsonBox>> fields() {
            if (!isObject()) {
                throw new IllegalStateException("Wrapped node is not an object");
            }
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            return new JsonNodeBoxIterator(it);
        }

        private static class JsonNodeBoxIterator implements Iterator<Map.Entry<String, JsonBox>> {
            private final Iterator<Map.Entry<String, JsonNode>> it;

            public JsonNodeBoxIterator(Iterator<Map.Entry<String, JsonNode>> it) {
                this.it = it;
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Map.Entry<String, JsonBox> next() {
                Map.Entry<String, JsonNode> entry = it.next();
                return new AbstractMap.SimpleEntry<>(entry.getKey(), new JsonBox(entry.getValue()));
            }
        }
    }
}
