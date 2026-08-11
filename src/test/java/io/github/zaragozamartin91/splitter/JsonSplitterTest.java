package io.github.zaragozamartin91.splitter;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class JsonSplitterTest {

    private final JsonCodec jsonCodec = JsonCodec.instance();

    @Test
    void testSplitByChunkSize() throws Exception {
        // GIVEN
        String json = TestUtil.utf8FileText("/sample-data.json");
        JsonSource source = new JsonSource(json);
        JsonSplitterConfig config = JsonSplitterConfig.splitByChunkSize(112, 144);
        JsonSplitter splitter = new JsonSplitter(config);

        long originalSize = jsonCodec.sizeInBytes(json);

        // WHEN
        SplitJson result = splitter.apply(source);
        List<JsonPart> parts = result.getParts();

        // THEN
        assertNotNull(parts);
        assertFalse(parts.isEmpty());

        long totalPartsSize = parts.stream()
                .mapToLong(p -> p.jsonMap() != null ? jsonCodec.sizeInBytes(p.jsonMap()) : jsonCodec.sizeInBytes(p.jsonArray()))
                .sum();

        assertTrue(Math.abs(originalSize - totalPartsSize) < 1000,
                "Total size should be roughly equal to original. Diff: " + Math.abs(originalSize - totalPartsSize));
    }

    @Test
    void testFlattenAndSplitByChunkSize() throws Exception {
        // GIVEN
        String json = TestUtil.utf8FileText("/sample-data-big.json");
        JsonSource source = new JsonSource(json);
        JsonSplitterConfig config = JsonSplitterConfig.splitByChunkSize(224, 288).withFlatten(true);
        JsonSplitter splitter = new JsonSplitter(config);

        long originalSize = jsonCodec.sizeInBytes(json);

        // WHEN
        SplitJson result = splitter.apply(source);
        List<JsonPart> parts = result.getParts();

        // THEN
        assertNotNull(parts);
        assertFalse(parts.isEmpty());

        long totalPartsSize = parts.stream()
                .mapToLong(p -> p.jsonMap() != null ? jsonCodec.sizeInBytes(p.jsonMap()) : jsonCodec.sizeInBytes(p.jsonArray()))
                .sum();

        assertTrue(Math.abs(originalSize - totalPartsSize) < 2000,
                "Total size should be roughly equal to original. Diff: " + Math.abs(originalSize - totalPartsSize));
    }

    @Test
    void testSplitByEntryCount_validWithRemainder() {
        // GIVEN
        String json = "{\"k1\":\"v1\", \"k2\":\"v2\", \"k3\":\"v3\"}";
        JsonSource source = new JsonSource(json);
        JsonSplitterConfig config = JsonSplitterConfig.splitByEntryCount(2);
        JsonSplitter splitter = new JsonSplitter(config);

        // WHEN
        SplitJson result = splitter.apply(source);
        List<JsonPart> parts = result.getParts();

        // THEN
        assertEquals(2, parts.size());
        assertEquals(2, parts.get(0).jsonMap().size());
        assertEquals(1, parts.get(1).jsonMap().size());
        assertTrue(parts.get(1).jsonMap().containsKey("k3"));
    }

    @Test
    void testSplitByNumberOfParts_valid() {
        // GIVEN
        String json = "{\"k1\":\"v1\", \"k2\":\"v2\", \"k3\":\"v3\", \"k4\":\"v4\"}";
        JsonSource source = new JsonSource(json);
        JsonSplitterConfig config = JsonSplitterConfig.splitByNumberOfParts(2);
        JsonSplitter splitter = new JsonSplitter(config);

        // WHEN
        SplitJson result = splitter.apply(source);
        List<JsonPart> parts = result.getParts();

        // THEN
        assertEquals(2, parts.size());
        assertEquals(2, parts.get(0).jsonMap().size());
        assertEquals(2, parts.get(1).jsonMap().size());
    }

    @Test
    void testSplitByEntryCount_withSampleData() throws Exception {
        // GIVEN
        String json = TestUtil.utf8FileText("/sample-data.json");
        JsonSource source = new JsonSource(json);
        JsonSplitterConfig config = JsonSplitterConfig.splitByEntryCount(6);
        JsonSplitter splitter = new JsonSplitter(config);
        int originalEntryCount = jsonCodec.readTree(json).size();

        // WHEN
        SplitJson result = splitter.apply(source);
        List<JsonPart> parts = result.getParts();

        // THEN
        assertEquals(4, parts.size());
        assertEquals(6, parts.get(0).jsonMap().size());
        assertEquals(6, parts.get(1).jsonMap().size());
        assertEquals(6, parts.get(2).jsonMap().size());
        assertEquals(4, parts.get(3).jsonMap().size());

        int totalEntries = parts.stream()
                .mapToInt(p -> p.jsonMap().size())
                .sum();
        assertEquals(originalEntryCount, totalEntries);

        Set<String> originalKeys = jsonCodec.polyReadMap(json).keySet();
        Set<String> resultKeys = parts.stream()
                .flatMap(p -> p.jsonMap().keySet().stream())
                .collect(Collectors.toSet());
        assertEquals(originalKeys, resultKeys);
    }

    @Test
    void testSplitByNumberOfParts_withSampleData() throws Exception {
        // GIVEN
        String json = TestUtil.utf8FileText("/sample-data.json");
        JsonSource source = new JsonSource(json);
        JsonSplitterConfig config = JsonSplitterConfig.splitByNumberOfParts(4);
        JsonSplitter splitter = new JsonSplitter(config);
        int originalEntryCount = jsonCodec.readTree(json).size();

        // WHEN
        SplitJson result = splitter.apply(source);
        List<JsonPart> parts = result.getParts();

        // THEN
        assertEquals(4, parts.size());
        assertEquals(6, parts.get(0).jsonMap().size());
        assertEquals(6, parts.get(1).jsonMap().size());
        assertEquals(6, parts.get(2).jsonMap().size());
        assertEquals(4, parts.get(3).jsonMap().size());

        int totalEntries = parts.stream()
                .mapToInt(p -> p.jsonMap().size())
                .sum();
        assertEquals(originalEntryCount, totalEntries);

        Set<String> originalKeys = jsonCodec.polyReadMap(json).keySet();
        Set<String> resultKeys = parts.stream()
                .flatMap(p -> p.jsonMap().keySet().stream())
                .collect(Collectors.toSet());
        assertEquals(originalKeys, resultKeys);
    }
}
