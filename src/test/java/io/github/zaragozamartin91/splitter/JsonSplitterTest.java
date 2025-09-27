package io.github.zaragozamartin91.splitter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/* Using java 8 & junit 5 ==> unit tests must hold the 'test' prefix */
public class JsonSplitterTest {

    @Test
    public void testTestDataFileIsReadableFromResources() throws URISyntaxException, IOException {
        String resourcePath = "/sample-data.json";
        String string = utf8FileText(resourcePath);
        assertNotNull(string);
        assertFalse(string.isEmpty());
    }

    @Test
    public void testSplitJsonEquallyPlusPreSortByEntryKey() throws URISyntaxException, IOException {
        String fileText = utf8FileText("/sample-data.json");

        JsonSource jsonSource = JsonSource.fromString(fileText);
        JsonSplitter jsonSplitter = new JsonSplitter(jsonSource);
        SplitStrategy strategy = DynamicSplitStrategy
                .equally(2) // Split equally into 2 parts
                .preSortByKey(SortFunction.SortOrder.ASCENDING); // Pre-sort entries by key in ascending order
        JsonOutput splitJson = jsonSplitter.split(strategy);
    }

    private URL resourceUrl(String resourcePath) {
        return this.getClass().getResource(resourcePath);
    }

    private String utf8FileText(String resourcePath) throws URISyntaxException, IOException {
        URL url = resourceUrl(resourcePath);
        Path path = Paths.get(url.toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(fileBytes, StandardCharsets.UTF_8);
    }
}
