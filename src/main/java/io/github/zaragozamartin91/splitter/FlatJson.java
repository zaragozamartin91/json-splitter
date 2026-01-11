package io.github.zaragozamartin91.splitter;

import java.util.Map;
import java.util.Set;
import com.github.wnameless.json.unflattener.JsonUnflattener;

public class FlatJson {
    private final Map<String, Object> data;

    public FlatJson(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> unflattenAsMap() {
        return JsonUnflattener.unflattenAsMap(getData());
    }

    public String unflattenAsString() {
        return JsonUnflattener.unflatten(getData());
    }

    Map<String, Object> getData() {
        return data;
    }

    Set<String> getKeySet() {
        return data.keySet();
    }
}
