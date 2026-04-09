package io.github.zaragozamartin91.splitter;

import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.*;

public class GroupFunctionTest {

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
}
