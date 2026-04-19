package io.github.zaragozamartin91.splitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class GroupFunctionTest {
    private static final Logger log = Logger.getLogger(GroupFunctionTest.class.getName());

    @Test
    public void testGroupByEntryCountYieldsAnEmptyListIfAnEmptyListIsPassed() {
        // GIVEN
        List<Entry<String, Object>> emptyEntries = new ArrayList<>();
        int maxEntries = 5;

        // WHEN
        List<List<Entry<String, Object>>> result = GroupFunction.groupByEntryCount(maxEntries).apply(emptyEntries);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGroupByEntryCountGroupsEntriesByNumberOfEntries() {
        // GIVEN
        List<Entry<String, Object>> entries = new ArrayList<>();
        entries.add(new AbstractMap.SimpleEntry<>("key1", "value1"));
        entries.add(new AbstractMap.SimpleEntry<>("key2", "value2"));
        entries.add(new AbstractMap.SimpleEntry<>("key3", "value3"));
        entries.add(new AbstractMap.SimpleEntry<>("key4", "value4"));
        entries.add(new AbstractMap.SimpleEntry<>("key5", "value5"));
        int maxEntries = 2;

        // WHEN
        List<List<Entry<String, Object>>> result = GroupFunction.groupByEntryCount(maxEntries).apply(entries);

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // First group should have 2 entries
        assertEquals(2, result.get(0).size());
        assertEquals("key1", result.get(0).get(0).getKey());
        assertEquals("key2", result.get(0).get(1).getKey());
        
        // Second group should have 2 entries
        assertEquals(2, result.get(1).size());
        assertEquals("key3", result.get(1).get(0).getKey());
        assertEquals("key4", result.get(1).get(1).getKey());
        
        // Third group should have 1 entry (remainder)
        assertEquals(1, result.get(2).size());
        assertEquals("key5", result.get(2).get(0).getKey());
    }

    @Test
    public void testGroupEquallySplitsTenEntriesIntoThreeParts() {
        // GIVEN
        List<Entry<String, Object>> sortedEntries = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            sortedEntries.add(new AbstractMap.SimpleEntry<>("key" + i, "value" + i));
        }
        int parts = 3;

        // WHEN
        List<List<Entry<String, Object>>> result = GroupFunction.groupEqually(parts).apply(sortedEntries);

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size());

        // Validate that the sum of all parts equals the original 10 entries
        long totalEntries = result.stream().mapToLong(List::size).sum();
        assertEquals(10, totalEntries);
    }

    @Test
    public void testGroupBySizeSplitsSampleDataIntoChunksOf120Bytes() throws IOException {
        // GIVEN
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sample-data.json");
        assertNotNull(inputStream, "sample-data.json should exist in test resources");
        Map<String, Object> sampleData = mapper.readValue(inputStream, Map.class);
        List<Entry<String, Object>> expectedEntries = new ArrayList<>(sampleData.entrySet());
        long sizeInBytes = 120;
        GroupBySizeContext context = new GroupBySizeContext(sizeInBytes);

        // WHEN
        List<List<Entry<String, Object>>> result = GroupFunction.groupBySize(context).apply(expectedEntries);

        // THEN
        assertNotNull(result);
        assertFalse(result.isEmpty(), "Result should contain at least one chunk");

        // Verify all entries are preserved
        long resultEntries = result.stream().mapToLong(List::size).sum();
        assertEquals(expectedEntries.size(), resultEntries, "All entries should be preserved across chunks");

        // Verify each chunk (except possibly the last) is within the size limit
        for (int i = 0; i < result.size(); i++) {
            List<Entry<String, Object>> chunk = result.get(i);
            assertFalse(chunk.isEmpty(), "Chunk " + i + " should not be empty");
            log.info("Part " + i + "=" + chunk);
        }

        Set<Entry<String, Object>> resultFlatEntries = result.stream().flatMap(ls -> ls.stream()).collect(Collectors.toSet());
        Set<Entry<String, Object>> expectedFlatEntries = expectedEntries.stream().collect(Collectors.toSet());
        assertEquals(expectedFlatEntries, resultFlatEntries, "All entries should be preserved across chunks");
    }
}
