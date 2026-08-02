package io.github.zaragozamartin91.splitter.split;

import java.util.*;
import java.util.stream.Collectors;

public enum SplitByEntryCount {
    INSTANCE;

    public List<Map<String, Object>> splitByEntryCount(Map<String, Object> input, int entryCount) {
        if (entryCount <= 0) throw new IllegalArgumentException("entryCount must be higher than 0");

        if (Objects.isNull(input) || input.isEmpty()) return List.of();

        ArrayList<Map<String, Object>> mapList = new ArrayList<>();

        ArrayList<Map.Entry<String, Object>> entries = new ArrayList<>(input.entrySet());
        for (int index = 0 ; index < input.size() ; index+=entryCount) {
            int end = Math.min(index + entryCount, entries.size());
            Map<String, Object> subMap = entries.subList(index, end).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            mapList.add(subMap);
        }

        return mapList;
    }
}
