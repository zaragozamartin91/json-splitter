package io.github.zaragozamartin91.splitter;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SplitByEntryCountTest {

    @Test
    public void testSplitByEntryCount_validExactMultiple() {
        // GIVEN
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("k1", "v1");
        input.put("k2", "v2");
        input.put("k3", "v3");
        input.put("k4", "v4");
        int entryCount = 2;

        // WHEN
        List<Map<String, Object>> result = SplitByEntryCount.INSTANCE.splitByEntryCount(input, entryCount);

        // THEN
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).size());
        assertEquals(2, result.get(1).size());
        assertTrue(result.get(0).containsKey("k1"));
        assertTrue(result.get(0).containsKey("k2"));
        assertTrue(result.get(1).containsKey("k3"));
        assertTrue(result.get(1).containsKey("k4"));
    }

    @Test
    public void testSplitByEntryCount_validWithRemainder() {
        // GIVEN
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("k1", "v1");
        input.put("k2", "v2");
        input.put("k3", "v3");
        int entryCount = 2;

        // WHEN
        List<Map<String, Object>> result = SplitByEntryCount.INSTANCE.splitByEntryCount(input, entryCount);

        // THEN
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).size());
        assertEquals(1, result.get(1).size());
        assertTrue(result.get(1).containsKey("k3"));
    }

    @Test
    public void testSplitByEntryCount_invalidEntryCount() {
        // GIVEN
        Map<String, Object> input = Map.of("k1", "v1");
        int entryCount = 0;

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
            SplitByEntryCount.INSTANCE.splitByEntryCount(input, entryCount)
        );
    }

    @Test
    public void testSplitByEntryCount_nullInput() {
        // WHEN
        List<Map<String, Object>> result = SplitByEntryCount.INSTANCE.splitByEntryCount(null, 5);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSplitByEntryCount_emptyInput() {
        // GIVEN
        Map<String, Object> input = Collections.emptyMap();

        // WHEN
        List<Map<String, Object>> result = SplitByEntryCount.INSTANCE.splitByEntryCount(input, 5);

        // THEN
        assertTrue(result.isEmpty());
    }
}
