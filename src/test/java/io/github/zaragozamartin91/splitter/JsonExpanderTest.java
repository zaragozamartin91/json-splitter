package io.github.zaragozamartin91.splitter;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class JsonExpanderTest {

    @Test
    public void testSimpleUnflatten() {
        Map<String, Object> flatMap = new LinkedHashMap<>();
        flatMap.put("name", "John");
        flatMap.put("age", 30);

        JsonExpander unflattener = new JsonExpander();
        Map<String, Object> result = unflattener.unflatten(flatMap).jsonMap();

        assertEquals("John", result.get("name"));
        assertEquals(30, result.get("age"));
    }

    @Test
    public void testNestedUnflatten() {
        Map<String, Object> flatMap = new LinkedHashMap<>();
        flatMap.put("user.name", "John");
        flatMap.put("user.address.city", "New York");
        flatMap.put("user.address.zip", "10001");

        JsonExpander unflattener = new JsonExpander();
        Map<String, Object> result = unflattener.unflatten(flatMap).jsonMap();

        assertTrue(result.get("user") instanceof Map);
        Map<String, Object> user = (Map<String, Object>) result.get("user");
        assertEquals("John", user.get("name"));
        assertTrue(user.get("address") instanceof Map);
        Map<String, Object> address = (Map<String, Object>) user.get("address");
        assertEquals("New York", address.get("city"));
        assertEquals("10001", address.get("zip"));
    }

    @Test
    public void testArrayUnflatten() {
        Map<String, Object> flatMap = new LinkedHashMap<>();
        flatMap.put("tags[0]", "java");
        flatMap.put("tags[1]", "json");

        JsonExpander unflattener = new JsonExpander();
        Map<String, Object> result = unflattener.unflatten(flatMap).jsonMap();

        assertTrue(result.get("tags") instanceof List);
        List<Object> tags = (List<Object>) result.get("tags");
        assertEquals(2, tags.size());
        assertEquals("java", tags.get(0));
        assertEquals("json", tags.get(1));
    }

    @Test
    public void testComplexUnflatten() {
        Map<String, Object> flatMap = new LinkedHashMap<>();
        flatMap.put("user.name", "John");
        flatMap.put("user.phones[0].number", "12345");
        flatMap.put("user.phones[0].type", "home");
        flatMap.put("user.phones[1].number", "67890");
        flatMap.put("user.phones[1].type", "work");
        flatMap.put("user.address.city", "New York");

        JsonExpander unflattener = new JsonExpander();
        Map<String, Object> result = unflattener.unflatten(flatMap).jsonMap();

        Map<String, Object> user = (Map<String, Object>) result.get("user");
        List<Object> phones = (List<Object>) user.get("phones");
        assertEquals(2, phones.size());

        Map<String, Object> phone0 = (Map<String, Object>) phones.get(0);
        assertEquals("12345", phone0.get("number"));
        assertEquals("home", phone0.get("type"));

        Map<String, Object> phone1 = (Map<String, Object>) phones.get(1);
        assertEquals("67890", phone1.get("number"));
        assertEquals("work", phone1.get("type"));
    }

    @Test
    public void testRootArrayUnflatten() throws Exception {
        String json = "[{\"name\":\"item1\"} , {\"name\":\"item2\"}]";

        JsonFlattener flattener = new JsonFlattener();
        Map<String, Object> flat = flattener.flatten(json).jsonMap();

        JsonExpander unflattener = new JsonExpander();
        JsonPart result = unflattener.unflatten(flat);

        // Root is always Map per signature. Root will have keys "[0]" and "[1]".
        assertEquals(2, result.jsonArray().size());
        Map<String, Object> item1 = (Map<String, Object>) result.jsonArray().get(0);
        assertEquals("item1", item1.get("name"));
        Map<String, Object> item2 = (Map<String, Object>) result.jsonArray().get(1);
        assertEquals("item2", item2.get("name"));

        assertEquals(JsonRootType.JSON_ARRAY, result.rootType());
    }

    @Test
    public void testPureArrayUnflatten() throws Exception {
        String json = "[\"apple\", \"banana\", \"cherry\"]";

        JsonFlattener flattener = new JsonFlattener();
        Map<String, Object> flat = flattener.flatten(json).jsonMap();

        JsonExpander unflattener = new JsonExpander();
        JsonPart result = unflattener.unflatten(flat);

        assertEquals(JsonRootType.JSON_ARRAY, result.rootType());
        assertEquals(3, result.jsonArray().size());
        assertEquals("apple", result.jsonArray().get(0));
        assertEquals("banana", result.jsonArray().get(1));
        assertEquals("cherry", result.jsonArray().get(2));
    }

    @Test
    public void testIntegrationWithFlattener() throws Exception {
        String json = "{\"name\":\"John\",\"address\":{\"city\":\"New York\",\"zip\":\"10001\"},\"tags\":[\"java\",\"json\"],\"phones\":[{\"number\":\"123\",\"type\":\"home\"},{\"number\":\"456\",\"type\":\"work\"}]}";

        JsonFlattener flattener = new JsonFlattener();
        Map<String, Object> flat = flattener.flatten(json).jsonMap();

        JsonExpander unflattener = new JsonExpander();
        JsonPart unflatten = unflattener.unflatten(flat);
        Map<String, Object> result = unflatten.jsonMap();

        assertEquals("John", result.get("name"));
        Map<String, Object> address = (Map<String, Object>) result.get("address");
        assertEquals("New York", address.get("city"));
        List<Object> tags = (List<Object>) result.get("tags");
        assertEquals("java", tags.get(0));
        List<Object> phones = (List<Object>) result.get("phones");
        Map<String, Object> phone0 = (Map<String, Object>) phones.get(0);
        assertEquals("123", phone0.get("number"));

        assertEquals(JsonRootType.JSON_MAP, unflatten.rootType());
    }
}
