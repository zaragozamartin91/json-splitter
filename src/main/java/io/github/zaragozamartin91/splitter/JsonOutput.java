package io.github.zaragozamartin91.splitter;

import java.util.List;
import java.util.Map;

import com.github.wnameless.json.unflattener.JsonUnflattener;

public class JsonOutput {
    private final List<JsonSource> parts;

    public JsonOutput(List<JsonSource> parts) {
        this.parts = parts;
    }

    
}
