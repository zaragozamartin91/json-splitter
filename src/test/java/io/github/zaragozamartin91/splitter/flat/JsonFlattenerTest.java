package io.github.zaragozamartin91.splitter.flat;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonFlattenerTest {
    private final JsonFlattener flattener = new JsonFlattener();

    @Test
    void testFlattenSimple() throws Exception {
        String json = "{\"name\": \"John\", \"age\": 30}";
        Map<String, Object> result = flattener.flatten(json);

        assertEquals(2, result.size());
        assertEquals("John", result.get("name"));
        assertEquals(30, result.get("age"));
    }

    @Test
    void testFlattenNestedObject() throws Exception {
        String json = "{\"user\": {\"id\": 1, \"profile\": {\"name\": \"John\"}}}";
        Map<String, Object> result = flattener.flatten(json);

        assertEquals(2, result.size());
        assertEquals(1, result.get("user.id"));
        assertEquals("John", result.get("user.profile.name"));
    }

    @Test
    void testFlattenArray() throws Exception {
        String json = "{\"tags\": [\"java\", \"json\"]}";
        Map<String, Object> result = flattener.flatten(json);

        assertEquals(2, result.size());
        assertEquals("java", result.get("tags[0]"));
        assertEquals("json", result.get("tags[1]"));
    }

    @Test
    void testFlattenNestedArrayOfObjects() throws Exception {
        String json = "{\"friends\": [{\"id\": 0, \"name\": \"Alice\"}, {\"id\": 1, \"name\": \"Bob\"}]}";
        Map<String, Object> result = flattener.flatten(json);

        assertEquals(4, result.size());
        assertEquals(0, result.get("friends[0].id"));
        assertEquals("Alice", result.get("friends[0].name"));
        assertEquals(1, result.get("friends[1].id"));
        assertEquals("Bob", result.get("friends[1].name"));
    }

    @Test
    void testFlattenNullValues() throws Exception {
        String json = "{\"a\": null, \"b\": \"value\"}";

        // Test default: keepNulls = true
        Map<String, Object> resultWithNulls = flattener.flatten(json);
        assertTrue(resultWithNulls.containsKey("a"), "Should keep nulls by default");
        assertEquals("value", resultWithNulls.get("b"));

        // Test discardNulls()
        Map<String, Object> resultWithoutNulls = new JsonFlattener().discardNulls().flatten(json);
        assertEquals(1, resultWithoutNulls.size());
        assertFalse(resultWithoutNulls.containsKey("a"), "Should discard nulls when requested");
        assertEquals("value", resultWithoutNulls.get("b"));
    }

    @Test
    void testFlattenMixedTypes() throws Exception {
        String json = "{\"bool\": true, \"long\": 1234567890123, \"double\": 12.34, \"int\": 42, \"text\": \"hello\"}";
        Map<String, Object> result = flattener.flatten(json);

        assertEquals(true, result.get("bool"));
        assertEquals(1234567890123L, result.get("long"));
        assertEquals(12.34, result.get("double"));
        assertEquals(42, result.get("int"));
        assertEquals("hello", result.get("text"));
    }

    @Test
    void testFlattenWithFixture() throws Exception {
        String json = new String(Files.readAllBytes(Paths.get("src/test/resources/sample-data.json")));
        Map<String, Object> result = flattener.flatten(json);

        assertNotNull(result);
        assertEquals("Randall Trujillo", result.get("name"));
        assertEquals(27, result.get("age"));
        assertEquals("blue", result.get("eyeColor"));
        assertEquals("cillum", result.get("tags[0]"));
        assertEquals("Chang Gentry", result.get("friends[0].name"));
        assertEquals(0, result.get("friends[0].id"));
        assertEquals(-35.446177, result.get("latitude"));
    }

    @Test
    void testFlattenWithComplexFixture() throws Exception {
        String json = new String(Files.readAllBytes(Paths.get("src/test/resources/complex-data.json")));
        Map<String, Object> result = flattener.flatten(json);

        assertNotNull(result);
        assertEquals("68b6a6ab8ac05f848f1e506d", result.get("_id"));
        assertEquals(0, result.get("index"));
        assertEquals("22f1253e-1390-4fe6-903f-9d58330993cb", result.get("guid"));
        assertEquals("DE89 3704 0044 0532 0130 00", result.get("bankAccounts[0].iban"));
        assertEquals(true, result.get("bankAccounts[0].isActive"));
        assertEquals("EUR", result.get("bankAccounts[0].balance.currency"));
        assertEquals(234500, result.get("bankAccounts[0].balance.amount"));
        assertEquals("SE12 9904 0044 0532 0130 00", result.get("bankAccounts[1].iban"));
        assertEquals(true, result.get("bankAccounts[1].isActive"));
        assertEquals("SEK", result.get("bankAccounts[1].balance.currency"));
        assertEquals(1250000, result.get("bankAccounts[1].balance.amount"));
        assertEquals("Randall Trujillo", result.get("name"));
        assertEquals("male", result.get("gender"));
        assertEquals("randalltrujillo@medicroix.com", result.get("email"));
        assertEquals("+1 (901) 478-2321", result.get("phone"));
        assertEquals(1.74, result.get("biometrics.height"));
        assertEquals("1995-06-26", result.get("biometrics.birthDate"));
        assertEquals("brown", result.get("biometrics.face.hairColor"));
        assertEquals("blue", result.get("biometrics.face.eyeColor"));
    }
}
