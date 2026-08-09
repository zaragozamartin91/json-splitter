package io.github.zaragozamartin91.splitter;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonCodecTest {
    private final JsonCodec reader = new JsonCodec();

    @Test
    void testReadTreeString() throws IOException {
        String json = "{\"key\": \"value\", \"num\": 123}";
        JsonCodec.JsonBox box = reader.readTree(json);
        assertTrue(box.isObject());
        assertEquals("value", box.node.get("key").asText());
        assertEquals(123, box.node.get("num").asInt());
    }

    @Test
    void testReadTreeFile() throws IOException {
        Path tempFile = Files.createTempFile("test-json", ".json");
        Files.writeString(tempFile, "{\"key\": \"file-value\"}");

        JsonCodec.JsonBox box = reader.readTree(tempFile.toFile());
        assertTrue(box.isObject());
        assertEquals("file-value", box.node.get("key").asText());

        Files.deleteIfExists(tempFile);
    }

    @Test
    void testValueToTree() {
        Map<String, Object> map = Map.of("key", "map-value", "num", 456);
        JsonCodec.JsonBox box = reader.valueToTree(map);
        assertTrue(box.isObject());
        assertEquals("map-value", box.node.get("key").asText());
        assertEquals(456, box.node.get("num").asInt());
    }

    @Test
    void testPolyReadTree() throws IOException {
        // String
        String json = "{\"key\": \"poly-string\"}";
        assertEquals("poly-string", reader.polyReadTree(json).node.get("key").asText());

        // File
        Path tempFile = Files.createTempFile("poly-test", ".json");
        Files.writeString(tempFile, "{\"key\": \"poly-file\"}");
        assertEquals("poly-file", reader.polyReadTree(tempFile.toFile()).node.get("key").asText());
        Files.deleteIfExists(tempFile);

        // Map
        Map<String, Object> map = Map.of("key", "poly-map");
        assertEquals("poly-map", reader.polyReadTree(map).node.get("key").asText());
    }

    @Test
    void testPolyReadTreeUnsupported() {
        assertThrows(IllegalArgumentException.class, () -> reader.polyReadTree(123));
        assertThrows(IllegalArgumentException.class, () -> reader.polyReadTree(null));
    }

    @Test
    void testFieldsSuccess() throws IOException {
        String json = "{\"a\": 1, \"b\": \"two\"}";
        JsonCodec.JsonBox box = reader.readTree(json);

        Iterator<Map.Entry<String, JsonCodec.JsonBox>> fields = box.fields();

        assertTrue(fields.hasNext());
        Map.Entry<String, JsonCodec.JsonBox> entry1 = fields.next();
        assertTrue(fields.hasNext());
        Map.Entry<String, JsonCodec.JsonBox> entry2 = fields.next();
        assertFalse(fields.hasNext());

        if (entry1.getKey().equals("a")) {
            assertEquals(1, entry1.getValue().asInt());
            assertEquals("two", entry2.getValue().asText());
        } else {
            assertEquals("two", entry1.getValue().asText());
            assertEquals(1, entry2.getValue().asInt());
        }
    }

    @Test
    void testPolyReadMapSuccess() throws IOException {
        // String
        String json = "{\"key\": \"map-string\", \"num\": 1}";
        Map<String, Object> resString = reader.polyReadMap(json);
        assertEquals("map-string", resString.get("key"));
        assertEquals(1, resString.get("num"));

        // File
        Path tempFile = Files.createTempFile("poly-map-test", ".json");
        Files.writeString(tempFile, "{\"key\": \"map-file\"}");
        Map<String, Object> resFile = reader.polyReadMap(tempFile.toFile());
        assertEquals("map-file", resFile.get("key"));
        Files.deleteIfExists(tempFile);

        // Map
        Map<String, Object> inputMap = Map.of("key", "map-map");
        Map<String, Object> resMap = reader.polyReadMap(inputMap);
        assertEquals("map-map", resMap.get("key"));
    }

    @Test
    void testPolyReadMapFailure() {
        // JSON Array
        assertThrows(IllegalArgumentException.class, () -> reader.polyReadMap("[1, 2, 3]"));
        // JSON Value
        assertThrows(IllegalArgumentException.class, () -> reader.polyReadMap("\"just a string\""));
    }

    @Test
    void testWriteValueAsBytes() {
        Map<String, Object> map = Map.of("key", "value");
        byte[] bytes = reader.writeValueAsBytes(map);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        String res = new String(bytes);
        assertTrue(res.contains("\"key\"") && res.contains("\"value\""));
    }
}
