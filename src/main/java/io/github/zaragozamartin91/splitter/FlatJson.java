package io.github.zaragozamartin91.splitter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.wnameless.json.unflattener.JsonUnflattener;

public class FlatJson {
    private final Map<String, Object> data;

    public FlatJson(Map<String, Object> data) {
        this.data = data;
    }

    public SplitJson split(SplitStrategy strategy) {
        List<Map<String, Object>> splitData = strategy.split(data);
        List<FlatJson> flatJsons = splitData.stream()
                .map(sd -> new FlatJson(sd))
                .collect(Collectors.toList());
        return new SplitJson(flatJsons);
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
