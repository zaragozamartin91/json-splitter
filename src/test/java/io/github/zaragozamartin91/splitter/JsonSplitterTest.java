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
        JsonSplitter jsonSplitter = new JsonSplitter(jsonSource);
        SplitStrategy strategy = DynamicSplitStrategy
                .equally(2) // Split equally into 2 parts
                .preSortByKey(SortFunction.SortOrder.ASCENDING); // Pre-sort entries by key in ascending order
        SplitJson splitJson = jsonSplitter.split(strategy);

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

        fail(); // fail on purpose
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
