package io.github.zaragozamartin91.splitter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* Using java 8 & junit 5 ==> unit tests must hold the 'test' prefix */
public class JsonSplitterTest {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testTestDataFileIsReadableFromResources() throws URISyntaxException, IOException {
        String resourcePath = "/sample-data.json";
        String string = utf8FileText(resourcePath);
        assertNotNull(string);
        assertFalse(string.isEmpty());
    }

    @Test
    public void testSplitJsonEquallyPlusPreSortByEntryKey() throws URISyntaxException, IOException {
        // GIVEN
        String fileText = utf8FileText("/sample-data.json");
        // read fileText as a Map using ObjectMapper
        Map<String, Object> originalJsonMap = mapper.readValue(fileText, new TypeReference<Map<String, Object>>() {
        });

        Map<String, Object> flatten = JsonFlattener.flattenAsMap(fileText);
        int flatKeyCount = flatten.keySet().size();

        // WHEN
        JsonSource jsonSource = JsonSource.fromString(fileText);
        JsonSplitter jsonSplitter = new JsonSplitter();
        SplitStrategy strategy = DynamicSplitStrategy
                .equally(2) // Split equally into 2 parts
                .flatten() // flatten the nested keys and values
                .preSortByKey(SortFunction.SortOrder.ASCENDING); // Pre-sort entries by key in ascending order
        SplitJson splitJson = jsonSplitter.split(jsonSource, strategy);

        // THEN
        FlatJson part0 = splitJson.getPart(0);
        FlatJson part1 = splitJson.getPart(1);
        Set<String> keySet0 = part0.getKeySet();
        Set<String> keySet1 = part1.getKeySet();
        int part0KeyCount = keySet0.size();
        int part1KeyCount = keySet1.size();
        assertTrue(part0KeyCount >= part1KeyCount);
        assertEquals(flatKeyCount, part0KeyCount + part1KeyCount);

        // assert that keySet0 contains none of the keys in keySet1 and vice versa
        assertTrue(keySet0.stream().noneMatch(keySet1::contains));
        assertTrue(keySet1.stream().noneMatch(keySet0::contains));

        Map<String, Object> unflattenAsMap0 = part0.unflattenAsMap();
        Map<String, Object> unflattenAsMap1 = part1.unflattenAsMap();
        assertTrue(originalJsonMap.keySet().containsAll(unflattenAsMap0.keySet()));
        assertTrue(originalJsonMap.keySet().containsAll(unflattenAsMap1.keySet()));
    }

    @Test
    public void testSplitJsonByEntryCount() throws URISyntaxException, IOException {
        // Create a JsonSource object from a JSON string
        JsonSource source = JsonSource.fromString("{\"key1\": \"value1\", \"key2\": \"value2\", \"key3\": \"value3\"}");

        // Create a DynamicSplitStrategy object to split the JSON data by entry count
        DynamicSplitStrategy strategy = DynamicSplitStrategy.byEntryCount(2);

        // Create a JsonSplitter object and split the JSON data
        JsonSplitter splitter = new JsonSplitter();
        SplitJson splitJson = splitter.split(source, strategy);

        // Get the split JSON data
        List<FlatJson> flatJsons = splitJson.getParts();

        // Assert that the split JSON data has the expected number of parts
        assertEquals(2, flatJsons.size());

        // Assert that each part contains the expected number of entries
        FlatJson part0 = flatJsons.get(0);
        FlatJson part1 = flatJsons.get(1);
        assertEquals(2, part0.getKeySet().size());
        assertEquals(1, part1.getKeySet().size());

        HashMap<String, Object> expectedMap0 = new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };

        HashMap<String, Object> expectedMap1 = new HashMap<String, Object>() {
            {
                put("key3", "value3");
            }
        };

        assertEquals(expectedMap0, part0.unflattenAsMap());
        assertEquals(expectedMap1, part1.unflattenAsMap());
    }

    @Test
    public void testSplitJsonWithNestedKeysAndFlatteningByEntryCount() throws URISyntaxException, IOException {
        // Create a JsonSource object from a JSON string
        JsonSource source = JsonSource.fromString(
                "{\"key1\": \"value1\", \"key2\": \"value2\", \"parentKey\": {\"nestedKey0\" : \"nestedValue0\", \"nestedKey1\" : \"nestedValue1\" , \"nestedKey2\" : \"nestedValue2\"} }");

        // Create a DynamicSplitStrategy object to split the JSON data by entry count
        DynamicSplitStrategy strategy = DynamicSplitStrategy.byEntryCount(3).flatten();

        // Create a JsonSplitter object and split the JSON data
        JsonSplitter splitter = new JsonSplitter();
        SplitJson splitJson = splitter.split(source, strategy);

        // Get the split JSON data
        List<FlatJson> flatJsons = splitJson.getParts();

        // Assert that the split JSON data has the expected number of parts
        assertEquals(2, flatJsons.size());

        // Assert that each part contains the expected number of entries
        FlatJson part0 = flatJsons.get(0);
        FlatJson part1 = flatJsons.get(1);
        assertEquals(3, part0.getKeySet().size());
        assertEquals(2, part1.getKeySet().size());

        HashMap<String, Object> expectedMap0 = new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key2", "value2");
                put("parentKey", new HashMap<String, Object>() {
                    {
                        put("nestedKey0", "nestedValue0");
                    }
                });
            }
        };

        HashMap<String, Object> expectedMap1 = new HashMap<String, Object>() {
            {
                put("parentKey", new HashMap<String, Object>() {
                    {
                        put("nestedKey1", "nestedValue1");
                        put("nestedKey2", "nestedValue2");
                    }
                });
            }
        };

        assertEquals(expectedMap0, part0.unflattenAsMap());
        assertEquals(expectedMap1, part1.unflattenAsMap());
    }

    @Test
    public void testSplitJsonWithNestedKeysAndNoFlatteningByEntryCount() throws URISyntaxException, IOException {
        // Create a JsonSource object from a JSON string
        JsonSource source = JsonSource.fromString(
                "{\"key1\": \"value1\", \"key2\": \"value2\", \"parentKey\": {\"nestedKey0\" : \"nestedValue0\", \"nestedKey1\" : \"nestedValue1\"} }");

        // Create a DynamicSplitStrategy object to split the JSON data by entry count
        DynamicSplitStrategy strategy = DynamicSplitStrategy.byEntryCount(2);

        // Create a JsonSplitter object and split the JSON data
        JsonSplitter splitter = new JsonSplitter();
        SplitJson splitJson = splitter.split(source, strategy);

        // Get the split JSON data
        List<FlatJson> flatJsons = splitJson.getParts();

        // Assert that the split JSON data has the expected number of parts
        assertEquals(2, flatJsons.size());

        // Assert that each part contains the expected number of entries
        FlatJson part0 = flatJsons.get(0);
        FlatJson part1 = flatJsons.get(1);
        assertEquals(2, part0.getKeySet().size());
        assertEquals(1, part1.getKeySet().size());

        HashMap<String, Object> expectedMap0 = new HashMap<String, Object>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };

        HashMap<String, Object> expectedMap1 = new HashMap<String, Object>() {
            {
                put("parentKey", new HashMap<String, Object>() {
                    {
                        put("nestedKey0", "nestedValue0");
                        put("nestedKey1", "nestedValue1");
                    }
                });
            }
        };

        assertEquals(expectedMap0, part0.unflattenAsMap());
        assertEquals(expectedMap1, part1.unflattenAsMap());
    }

    private URL resourceUrl(String resourcePath) {
        return this.getClass().getResource(resourcePath);
    }

    private String utf8FileText(String resourcePath) throws URISyntaxException, IOException {
        URL url = resourceUrl(resourcePath);
        Path path = Paths.get(url.toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(fileBytes, StandardCharsets.UTF_8);
    }
}
