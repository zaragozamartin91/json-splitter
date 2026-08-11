package io.github.zaragozamartin91.splitter;

import java.util.Objects;
import java.util.function.Predicate;

import static io.github.zaragozamartin91.splitter.JsonSplitterStrategy.*;

@PublicApi
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

    JsonSplitterConfig() {
        this.flatten = false;
    }

    JsonSplitterConfig(JsonSplitterConfig other) {
        this.flatten = other.flatten;
        this.strategy = other.strategy;
        this.entryCount = other.entryCount;
        this.numberOfParts = other.numberOfParts;
        this.minSizeBytes = other.minSizeBytes;
        this.maxSizeBytes = other.maxSizeBytes;
    }

    /**
     * Sets whether the JSON should be flattened before splitting.
     * @param flatten True to enable flattening, false to disable
     * @return A new configuration instance with the updated flatten setting
     */
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

    /**
     * Creates a configuration that splits the JSON based on the number of entries.
     * @param entryCount The maximum number of entries per part
     * @return A configuration instance for entry count splitting
     */
    public static JsonSplitterConfig splitByEntryCount(int entryCount) {
        JsonSplitterConfig config = new JsonSplitterConfig().withStrategy(SPLIT_BY_ENTRY_COUNT);
        config.entryCount = entryCount;
        return config;
    }

    /**
     * Creates a configuration that splits the JSON into a specific number of parts.
     * @param numberOfParts The total number of parts to split into
     * @return A configuration instance for part count splitting
     */
    public static JsonSplitterConfig splitByNumberOfParts(int numberOfParts) {
        JsonSplitterConfig config = new JsonSplitterConfig().withStrategy(SPLIT_BY_NUMBER_OF_PARTS);
        config.numberOfParts = numberOfParts;
        return config;
    }

    /**
     * Creates a configuration that splits the JSON into chunks within a specified size range.
     * @param minSizeBytes The minimum size of each chunk in bytes
     * @param maxSizeBytes The maximum size of each chunk in bytes
     * @return A configuration instance for size-based splitting
     */
    public static JsonSplitterConfig splitByChunkSize(long minSizeBytes,
                                                      long maxSizeBytes) {
        JsonSplitterConfig config = new JsonSplitterConfig().withStrategy(SPLIT_BY_CHUNK_SIZE);
        config.minSizeBytes = minSizeBytes;
        config.maxSizeBytes = maxSizeBytes;
        return config;
    }

    Integer entryCount() {
        return entryCount;
    }

    Integer numberOfParts() {
        return numberOfParts;
    }

    Long minSizeBytes() {
        return minSizeBytes;
    }

    Long maxSizeBytes() {
        return maxSizeBytes;
    }

    JsonSplitterConfig valid() {
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
