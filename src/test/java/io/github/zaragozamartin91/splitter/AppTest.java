package io.github.zaragozamartin91.splitter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import com.github.wnameless.json.flattener.JsonFlattener;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void testFlatten() {
        String json = "{ \"a\" : { \"b\" : 1, \"c\": null, \"d\": [false, true] }, \"e\": \"f\", \"g\":2.3 }";

        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);

        System.out.println("flattenJson = " + flattenJson);
    }
}
