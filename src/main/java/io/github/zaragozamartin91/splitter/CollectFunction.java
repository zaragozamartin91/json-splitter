package io.github.zaragozamartin91.splitter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class CollectFunction {
    public static List<Map<String, Object>> collectToLinkedHashMap(List<List<Entry<String, Object>>> sortedGroups) {
        return sortedGroups.stream()
                .map(subList -> {
                    Map<String, Object> subMap = subList
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> {
                                throw new RuntimeException("Duplicate key found while collecting split entries" + e1);
                            }, LinkedHashMap::new));
                    return subMap;
                }).collect(Collectors.toList());
    }
}
