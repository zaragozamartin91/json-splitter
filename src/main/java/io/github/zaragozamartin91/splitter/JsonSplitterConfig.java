package io.github.zaragozamartin91.splitter;

import java.util.Objects;
import java.util.function.Predicate;

import static io.github.zaragozamartin91.splitter.JsonSplitterStrategy.*;

public class JsonSplitterConfig {
    private boolean flatten;
    private JsonSplitterStrategy strategy;

    /* splitByEntryCount */
    private Integer entryCount;

    /* splitByNumberOfParts */
    private Integer numberOfParts;

    /* splitByChunkSize */
    private Long minSizeBytes;
    private Long maxSizeBytes;

    public JsonSplitterConfig() {
        this.flatten = false;
    }

    public JsonSplitterConfig(JsonSplitterConfig other) {
        this.flatten = other.flatten;
        this.strategy = other.strategy;
        this.entryCount = other.entryCount;
        this.numberOfParts = other.numberOfParts;
        this.minSizeBytes = other.minSizeBytes;
        this.maxSizeBytes = other.maxSizeBytes;
    }

    public JsonSplitterConfig withFlatten(boolean flatten) {
        JsonSplitterConfig other = new JsonSplitterConfig(this);
        other.flatten = flatten;
        return other;
    }

    JsonSplitterConfig withStrategy(JsonSplitterStrategy strategy) {
        JsonSplitterConfig other = new JsonSplitterConfig(this);
        other.strategy = strategy;
        return other;
    }

    boolean flatten() {
        return flatten;
    }

    JsonSplitterStrategy strategy() {
        return strategy;
    }

    public static JsonSplitterConfig splitByEntryCount(int entryCount) {
        JsonSplitterConfig config = new JsonSplitterConfig().withStrategy(SPLIT_BY_ENTRY_COUNT);
        config.entryCount = entryCount;
        return config;
    }

    public static JsonSplitterConfig splitByNumberOfParts(int numberOfParts) {
        JsonSplitterConfig config = new JsonSplitterConfig().withStrategy(SPLIT_BY_NUMBER_OF_PARTS);
        config.numberOfParts = numberOfParts;
        return config;
    }

    public static JsonSplitterConfig splitByChunkSize(long minSizeBytes,
                                                      long maxSizeBytes) {
        JsonSplitterConfig config = new JsonSplitterConfig().withStrategy(SPLIT_BY_CHUNK_SIZE);
        config.minSizeBytes = minSizeBytes;
        config.maxSizeBytes = maxSizeBytes;
        return config;
    }

    public Integer entryCount() {
        return entryCount;
    }

    public Integer numberOfParts() {
        return numberOfParts;
    }

    public Long minSizeBytes() {
        return minSizeBytes;
    }

    public Long maxSizeBytes() {
        return maxSizeBytes;
    }

    public JsonSplitterConfig valid() {
        validateState(strategy, Objects::nonNull, "Missing splitting strategy");

        switch (strategy()) {
            case SPLIT_BY_CHUNK_SIZE:
                validateState(minSizeBytes, Objects::nonNull, "Missing minSizeBytes");
                validateState(minSizeBytes, v -> ((Long)v) > 0, "minSizeBytes must be bigger than 0");
                validateState(maxSizeBytes, Objects::nonNull, "Missing maxSizeBytes");
                validateState(maxSizeBytes, v -> ((Long)v) > 0, "maxSizeBytes must be bigger than 0");
                break;
            case SPLIT_BY_ENTRY_COUNT:
                validateState(entryCount, Objects::nonNull, "Missing entryCount");
                validateState(entryCount, v -> ((Integer)v) > 0, "entryCount must be bigger than 0");
                break;
            case SPLIT_BY_NUMBER_OF_PARTS:
                validateState(numberOfParts, Objects::nonNull, "Missing numberOfParts");
                validateState(numberOfParts, v -> ((Integer)v) > 0, "numberOfParts must be bigger than 0");
                break;
        }
        return this;
    }

    static void validateState(Object value, Predicate<Object> predicate, String message) {
        if (predicate.test(value)) return;
        throw new IllegalStateException(message);
    }
}
