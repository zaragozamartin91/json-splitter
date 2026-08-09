package io.github.zaragozamartin91.splitter;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SplitByNumberOfPartsTest {

    @Test
    public void testSplitByNumberOfParts_valid() {
        // GIVEN
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("k1", "v1");
        input.put("k2", "v2");
        input.put("k3", "v3");
        input.put("k4", "v4");
        int numberOfParts = 2;

        // WHEN
        List<Map<String, Object>> result = SplitByNumberOfParts.INSTANCE.splitByNumberOfParts(input, numberOfParts);

        // THEN
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).size());
        assertEquals(2, result.get(1).size());
    }

    @Test
    public void testSplitByNumberOfParts_singlePart() {
        // GIVEN
        Map<String, Object> input = Map.of("k1", "v1");
        int numberOfParts = 1;

        // WHEN
        List<Map<String, Object>> result = SplitByNumberOfParts.INSTANCE.splitByNumberOfParts(input, numberOfParts);

        // THEN
        assertEquals(1, result.size());
        assertEquals(input, result.get(0));
    }

    @Test
    public void testSplitByNumberOfParts_invalidParts() {
        // GIVEN
        Map<String, Object> input = Map.of("k1", "v1");
        int numberOfParts = 0;

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () ->
            SplitByNumberOfParts.INSTANCE.splitByNumberOfParts(input, numberOfParts)
        );
    }
}
