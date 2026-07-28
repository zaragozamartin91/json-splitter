package io.github.zaragozamartin91.splitter.split;

import java.util.*;

public enum SplitByEntryCount {
    INSTANCE;

    static class StringObjectMap extends HashMap<String, Object> {}

    public List<Map<String, Object>> splitByEntryCount(Map<String, Object> input, int entryCount) {
        if (entryCount <= 0) throw new IllegalArgumentException("entryCount must be higher than 0");

        if (Objects.isNull(input) || input.isEmpty()) return List.of();

        ArrayList<Map<String, Object>> mapList = new ArrayList<>();

        Map<String, Object> currentMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            currentMap.put(entry.getKey(), entry.getValue());
            if (currentMap.size() >= entryCount) {
                mapList.add(currentMap);
                currentMap = new HashMap<>();
            }
        }
        if (!currentMap.isEmpty()) {
            mapList.add(currentMap);
        }

        return mapList;
    }
}
