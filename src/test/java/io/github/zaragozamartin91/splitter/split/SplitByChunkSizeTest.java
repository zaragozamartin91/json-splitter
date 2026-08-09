package io.github.zaragozamartin91.splitter.split;

import io.github.zaragozamartin91.splitter.JsonCodec;
import io.github.zaragozamartin91.splitter.TestUtil;
import io.github.zaragozamartin91.splitter.flat.JsonFlattener;
import io.github.zaragozamartin91.splitter.flat.JsonExpander;
import io.github.zaragozamartin91.splitter.flat.ExpandedJson;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SplitByChunkSizeTest {

    private final JsonCodec mapper = JsonCodec.instance();

    @Test
    void testSplitByChunkSize() throws Exception {
        // Load fixture
        String json = TestUtil.utf8FileText("/sample-data.json");
        Map<String, Object> input = mapper.polyReadMap(json);

        // Size function: JSON byte length
        Function<Object, Long> sizeFunction = this::mapSizeAsBytes;

        long originalSize = sizeFunction.apply(input);

        // Split into chunks of approximately 128 bytes
        long targetSize = 128;
        int delta = 16;
        List<Map<String, Object>> result = SplitByChunkSize.INSTANCE.splitByChunkSize(
                input,
                targetSize - delta,
                targetSize + delta,
                sizeFunction,
                SplitByChunkSizeTest::newLinkedHashMap
        );

        assertNotNull(result);
        assertFalse(result.isEmpty(), "Result should not be empty");

        // Verify total size consistency
        long totalChunksSize = result.stream()
                .mapToLong(sizeFunction::apply)
                .sum();

        // JSON structural overhead changes when split into multiple maps
        // Original: { a:1, b:2, ... } -> Size S
        // Split: { a:1 }, { b:2 }, ... -> Sizes S1, S2 ...
        // The sum of S1, S2... will differ from S due to braces/commas.
        // We check if it's within a reasonable margin.
        assertTrue(Math.abs(originalSize - totalChunksSize) < 1000,
                "Total size of chunks should be roughly equal to original size. Diff: " + Math.abs(originalSize - totalChunksSize));

        /* All keys are kept */
        assertEquals(
                input.keySet(),
                result.stream().map(Map::keySet).flatMap(Collection::stream).collect(Collectors.toSet())
        );
    }



    @Test
    void testFlattenAndSplitByChunkSize() throws Exception {
        // Load fixture
        String json = TestUtil.utf8FileText("/sample-data-big.json");
        JsonFlattener jsonFlattener = new JsonFlattener(JsonCodec.instance());
        Map<String, Object> input = jsonFlattener.flatten(json).jsonMap();

        // Size function: JSON byte length
        Function<Object, Long> sizeFunction = this::flatMapSizeAsBytes;

        long originalSize = sizeFunction.apply(input);

        // Split into chunks of approximately 256 bytes
        long targetSize = 256;
        int delta = 32;
        List<Map<String, Object>> result = SplitByChunkSize.INSTANCE.splitByChunkSize(
                input,
                targetSize - delta,
                targetSize + delta,
                sizeFunction,
                SplitByChunkSizeTest::newLinkedHashMap
        );

        assertNotNull(result);
        assertFalse(result.isEmpty(), "Result should not be empty");

        // Verify total size consistency
        long totalChunksSize = result.stream().mapToLong(sizeFunction::apply).sum();

        // JSON structural overhead changes when split into multiple maps
        // Original: { a:1, b:2, ... } -> Size S
        // Split: { a:1 }, { b:2 }, ... -> Sizes S1, S2 ...
        // The sum of S1, S2... will differ from S due to braces/commas.
        // We check if it's within a reasonable margin.
        assertTrue(Math.abs(originalSize - totalChunksSize) < 1000,
                "Total size of chunks should be roughly equal to original size. Diff: " + Math.abs(originalSize - totalChunksSize));

        /* All keys are kept */
        assertEquals(
                input.keySet(),
                result.stream().map(Map::keySet).flatMap(Collection::stream).collect(Collectors.toSet())
        );
    }

    private long mapSizeAsBytes(Object obj) {
        JsonCodec jsonCodec = JsonCodec.instance();
        return JsonSize.mapSizeAsBytes(obj, jsonCodec::sizeInBytes);
    }

    private static LinkedHashMap<String, Object> newLinkedHashMap(Collection<Map.Entry<String, Object>> entries) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : entries) map.put(entry.getKey(), entry.getValue());
        return map;
    }

    private long flatMapSizeAsBytes(Object obj) {
        JsonCodec jsonCodec = JsonCodec.instance();
        return JsonSize.flatMapSizeAsBytes(obj, jsonCodec::sizeInBytes, SplitByChunkSizeTest::unflattenAndMap);
    }

    private static Map<String, Object> unflattenAndMap(Collection<Map.Entry<String, Object>> entries) {
        LinkedHashMap<String, Object> flatMap = newLinkedHashMap(entries);
        JsonExpander jsonExpander = new JsonExpander();
        ExpandedJson unflatten = jsonExpander.unflatten(flatMap);
        return unflatten.jsonMap();
    }

    @Test
    void testSizeIntervalValid() {
        assertDoesNotThrow(() -> new SizeInterval(100, 200));
    }

    @Test
    void testSizeIntervalSameSize() {
        assertDoesNotThrow(() -> new SizeInterval(100, 100));
    }

    @Test
    void testSizeIntervalMinZero() {
        assertThrows(IllegalArgumentException.class, () -> new SizeInterval(0, 200));
    }

    @Test
    void testSizeIntervalMinNegative() {
        assertThrows(IllegalArgumentException.class, () -> new SizeInterval(-1, 200));
    }

    @Test
    void testSizeIntervalMaxZero() {
        assertThrows(IllegalArgumentException.class, () -> new SizeInterval(100, 0));
    }

    @Test
    void testSizeIntervalMaxNegative() {
        assertThrows(IllegalArgumentException.class, () -> new SizeInterval(100, -1));
    }

    @Test
    void testSizeIntervalMaxLessThanMin() {
        assertThrows(IllegalArgumentException.class, () -> new SizeInterval(200, 100));
    }
}
