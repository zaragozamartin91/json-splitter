package io.github.zaragozamartin91.splitter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import com.github.wnameless.json.flattener.JsonFlattener;

/**
 * Unit test for simple App.
 */
public class JsonFlattenerSpike {

    private static final Logger log = Logger.getLogger(JsonFlattenerSpike.class.getName());

    @Test
    public void testFlattenSampleData() throws IOException, URISyntaxException {
        // GIVEN
        String json = TestUtil.utf8FileText("/sample-data.json");
        assertTrue(json != null && !json.isEmpty(), "sample-data.json should exist in test resources");

        // WHEN
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);

        // THEN
        log.info("flattenJson = " + flattenJson);
    }
}
