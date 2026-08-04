package io.github.zaragozamartin91.splitter.split;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zaragozamartin91.splitter.TestUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SplitByChunkSizeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testSplitByChunkSize() throws Exception {
        // Load fixture
        String json = TestUtil.utf8FileText("/sample-data.json");
        Map<String, Object> input = mapper.readValue(json, new TypeReference<>() {});

        // Size function: JSON byte length
        Function<Object, Long> sizeFunction = obj -> {
            try {
                return (long) mapper.writeValueAsBytes(obj).length;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        long originalSize = sizeFunction.apply(input);

        // Split into chunks of approximately 128 bytes
        long targetSize = 128;
        int delta = 16;
        List<Map<String, Object>> result = SplitByChunkSize.INSTANCE.splitByChunkSize(
                input,
                targetSize - delta,
                targetSize + delta,
                sizeFunction
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
