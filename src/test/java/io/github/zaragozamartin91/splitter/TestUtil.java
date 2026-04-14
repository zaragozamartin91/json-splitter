package io.github.zaragozamartin91.splitter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class TestUtil {

    static URL resourceUrl(String resourcePath) {
        return TestUtil.class.getResource(resourcePath);
    }

    static String utf8FileText(String resourcePath) throws URISyntaxException, IOException {
        URL url = resourceUrl(resourcePath);
        Path path = Paths.get(url.toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(fileBytes, StandardCharsets.UTF_8);
    }
}
