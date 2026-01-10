package io.github.zaragozamartin91.splitter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.github.wnameless.json.flattener.JsonFlattener;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void testFlatten() {
        String json = "{ \"a\" : { \"b\" : 1, \"c\": null, \"d\": [false, true] }, \"e\": \"f\", \"g\":2.3 }";

        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);

        System.out.println("flattenJson = " + flattenJson);
    }

        @Test
    public void testFlatten2() {
        String json = "{ \"a\" : { \"b\" : 1, \"c\": null, \"d\": [false, true] }, \"e\": \"f\", \"g\":2.3 }";

        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);

        System.out.println("flattenJson = " + flattenJson);
    }
}
