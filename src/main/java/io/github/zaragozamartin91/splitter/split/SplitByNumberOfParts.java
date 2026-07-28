package io.github.zaragozamartin91.splitter.split;

import java.util.*;

public enum SplitByNumberOfParts {
    INSTANCE;

    public List<Map<String, Object>> splitByNumberOfParts(Map<String, Object> input, int numberOfParts) {
        if (numberOfParts <= 0) throw new IllegalArgumentException("numberOfParts must be higher than 0");

        if (numberOfParts == 1) return List.of(input);

        int entryCount = (int) Math.ceil(((double) input.size()) / ((double) numberOfParts));

        return SplitByEntryCount.INSTANCE.splitByEntryCount(input, entryCount);
    }
}
