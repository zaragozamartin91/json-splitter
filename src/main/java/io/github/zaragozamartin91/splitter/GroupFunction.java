package io.github.zaragozamartin91.splitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    public static Function<List<Entry<String, Object>>, List<List<Entry<String, Object>>>> groupBySize(GroupBySizeContext context) {
        return (sortedEntries) -> groupBySize(sortedEntries, context.getSizeInBytes());
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

    private static final ObjectMapper SIZE_MAPPER = new ObjectMapper();

    private static List<List<Entry<String, Object>>> groupBySize(List<Entry<String, Object>> sortedEntries, long sizeInBytes) {
        List<List<Entry<String, Object>>> groups = new ArrayList<>();
        if (sortedEntries.isEmpty()) {
            return groups;
        }

        List<Entry<String, Object>> currentGroup = new ArrayList<>();
        for (Entry<String, Object> entry : sortedEntries) {
            currentGroup.add(entry);
            try {
                long serializedSize = measureSerializedSize(currentGroup);
                if (serializedSize > sizeInBytes) {
                    // If this is the first entry in the group, accept it anyway (oversized single entry)
                    if (currentGroup.size() == 1) {
                        groups.add(new ArrayList<>(currentGroup));
                        currentGroup.clear();
                    } else {
                        // Remove the last entry, finalize the current group, and start a new one with the removed entry
                        Entry<String, Object> overflowEntry = currentGroup.remove(currentGroup.size() - 1);
                        groups.add(new ArrayList<>(currentGroup));
                        currentGroup.clear();
                        currentGroup.add(overflowEntry);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to measure serialized size of entry group", e);
            }
        }

        // Add any remaining entries as the final group
        if (!currentGroup.isEmpty()) {
            groups.add(new ArrayList<>(currentGroup));
        }

        return groups;
    }

    /* FIXME : If the entry is flattened then the key size will be higher than normal */
    private static long measureSerializedSize(List<Entry<String, Object>> entries) throws Exception {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (Entry<String, Object> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        byte[] bytes = SIZE_MAPPER.writeValueAsBytes(map);
        return bytes.length;
    }
}
