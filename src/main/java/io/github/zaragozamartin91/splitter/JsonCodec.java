package io.github.zaragozamartin91.splitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

class JsonCodec {
    static JsonCodec instance() { return new JsonCodec(); }

    private final ObjectMapper mapper = new ObjectMapper();

    JsonBox readTree(String jsonString) {
        try {
            return new JsonBox(mapper.readTree(jsonString));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    JsonBox readTree(File file) {
        try {
            return new JsonBox(mapper.readTree(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    JsonBox valueToTree(Map<?, ?> map) {
        return new JsonBox(mapper.valueToTree(map));
    }

    JsonBox polyReadTree(Object input) {
        if (input instanceof String) return readTree((String) input);
        if (input instanceof File) return readTree((File) input);
        if (input instanceof Map) return valueToTree((Map<?, ?>) input);
        if (input instanceof JsonBox) return (JsonBox) input;

        throw new IllegalArgumentException(
            "Unsupported input type: " + (input == null ? "null" : input.getClass().getName())
        );
    }

    Map<String, Object> polyReadMap(Object input) {
        try {
            if (input instanceof String) return mapper.readValue((String) input, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Cannot read map out of %s", input),e);
        }
        try {
            if (input instanceof File) return mapper.readValue((File) input, new TypeReference<>() {});
        } catch (IOException e) {
            File theFile = (File) input;
            throw new IllegalArgumentException(String.format("Cannot read map out of %s", theFile.getAbsolutePath()), e);
        }
        if (input instanceof Map) return (Map<String, Object>) input;
        if (input instanceof JsonBox) return toMap((JsonBox) input);

        throw new IllegalArgumentException(String.format("Cannot read map out of %s type", input.getClass()));
    }

    Map<String, Object> toMap(JsonBox box) {
        if (!box.isObject()) throw new IllegalArgumentException("Wrapped node is not an object");
        return mapper.convertValue(box.node, Map.class);
    }

    byte[] writeValueAsBytes(Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    int sizeInBytes(Object value) {
        return writeValueAsBytes(value).length;
    }

    static class JsonBox {
        JsonNode node;

        JsonBox(JsonNode node) {
            this.node = node;
        }

        boolean isObject() { return node.isObject(); }
        boolean isArray() { return node.isArray(); }
        boolean isNull() { return node.isNull(); }
        boolean isValueNode() { return node.isValueNode(); }
        boolean isBoolean() { return node.isBoolean(); }
        boolean asBoolean() { return node.asBoolean(); }
        boolean isLong() { return node.isLong(); }
        long asLong() { return node.asLong(); }
        boolean isInt() { return node.isInt(); }
        int asInt() { return node.asInt(); }
        boolean isDouble() { return node.isDouble(); }
        double asDouble() { return node.asDouble(); }
        String asText() { return node.asText(); }

        JsonBox get(int index) {
            if (!isArray()) { throw new IllegalArgumentException("Wrapped node is not an array"); }
            return new JsonBox(node.get(index));
        }

        int size() {
            return node.size();
        }

        Iterator<Map.Entry<String, JsonBox>> fields() {
            if (!isObject()) {
                throw new IllegalStateException("Wrapped node is not an object");
            }
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            return new JsonNodeBoxIterator(it);
        }

        private static class JsonNodeBoxIterator implements Iterator<Map.Entry<String, JsonBox>> {
            private final Iterator<Map.Entry<String, JsonNode>> it;

            JsonNodeBoxIterator(Iterator<Map.Entry<String, JsonNode>> it) {
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
