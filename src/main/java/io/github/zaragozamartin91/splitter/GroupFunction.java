package io.github.zaragozamartin91.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

public class GroupFunction {
    public static List<List<Entry<String, Object>>> identity(List<Entry<String, Object>> sortedEntries) {
        List<List<Entry<String, Object>>> groups = new ArrayList<>();
        groups.add(sortedEntries);
        return groups;
    }
    
    public static Function<List<Entry<String, Object>>, List<List<Entry<String, Object>>>> groupByEntryCount(int maxEntries) {
        return (sortedEntries) -> groupByEntryCount(sortedEntries, maxEntries);
    }
    
    public static Function<List<Entry<String, Object>>, List<List<Entry<String, Object>>>> groupEqually(int parts) {
        return (sortedEntries) -> groupEqually(sortedEntries, parts);
    }

    private static List<List<Entry<String, Object>>> groupByEntryCount(List<Entry<String, Object>> sortedEntries, int maxEntries) {
        List<List<Entry<String, Object>>> groups = new ArrayList<>();
        for (int startIndex = 0; startIndex < sortedEntries.size(); startIndex += maxEntries) {
            int endIndex = Math.min(startIndex + maxEntries, sortedEntries.size());
            List<Entry<String, Object>> subList = sortedEntries.subList(startIndex, endIndex);
            groups.add(subList);
        }
        return groups;
    }

    private static List<List<Entry<String, Object>>> groupEqually(List<Entry<String, Object>> sortedEntries, int parts) {
        int totalEntries = sortedEntries.size();
        int maxEntries = (int) Math.ceil((double) totalEntries / parts);
        return groupByEntryCount(sortedEntries, maxEntries);
    }
}
