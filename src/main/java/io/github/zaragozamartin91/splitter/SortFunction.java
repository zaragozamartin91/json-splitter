package io.github.zaragozamartin91.splitter;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SortFunction {
    enum SortOrder {
        ASCENDING,
        DESCENDING
    }

    public static List<Entry<String, Object>> identity(Collection<Entry<String, Object>> entrySet) {
        return entrySet.stream().collect(Collectors.toList());
    }

    public static List<Entry<String, Object>> sortByKeyAsc(Collection<Entry<String, Object>> entrySet) {
        return entrySet
                .stream()
                .sorted(Comparator.comparing((Map.Entry<String, Object> entry) -> entry.getKey()))
                .collect(Collectors.toList());
    }

    public static List<Entry<String, Object>> sortByKeyDesc(Collection<Entry<String, Object>> entrySet) {
        return entrySet
                .stream()
                .sorted(Comparator.comparing((Map.Entry<String, Object> entry) -> entry.getKey()).reversed())
                .collect(Collectors.toList());
    }

    public static Function<Collection<Entry<String, Object>>, List<Entry<String, Object>>> sortByKey(SortOrder order) {
        switch (order) {
            case ASCENDING:
                return SortFunction::sortByKeyAsc;
            case DESCENDING:
                return SortFunction::sortByKeyDesc;
            default:
                throw new IllegalArgumentException("Unsupported sort order: " + order);
        }
    }
}
