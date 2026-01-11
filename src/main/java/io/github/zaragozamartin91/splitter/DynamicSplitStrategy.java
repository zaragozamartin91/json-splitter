package io.github.zaragozamartin91.splitter;

import java.util.List;
import java.util.Map;

import io.github.zaragozamartin91.splitter.SortFunction.SortOrder;

public class DynamicSplitStrategy implements SplitStrategy {
    private ModularSplitStrategy modularStrategy;

    public DynamicSplitStrategy(ModularSplitStrategy modularStrategy) {
        this.modularStrategy = modularStrategy;
    }

    @Override
    public List<Map<String, Object>> split(JsonUnit jsonUnit) {
        return modularStrategy.split(jsonUnit);
    }

    /*
     * Factory methods
     * -----------------------------------------------------------------------------
     */

    /* Creates a split strategy that splits json data by entry count */
    public static DynamicSplitStrategy byEntryCount(int entryCount) {
        return new DynamicSplitStrategy(
                new ModularSplitStrategy(
                        false,
                        SortFunction::identity,
                        GroupFunction.groupByEntryCount(entryCount),
                        CollectFunction::collectToLinkedHashMap,
                        SortFunction::identity));
    }

    public static DynamicSplitStrategy equally(int parts) {
        return new DynamicSplitStrategy(
                new ModularSplitStrategy(
                        false,
                        SortFunction::identity,
                        GroupFunction.groupEqually(parts),
                        CollectFunction::collectToLinkedHashMap,
                        SortFunction::identity));
    }

    /*
     * Modification methods
     * -----------------------------------------------------------------------------
     */

    public DynamicSplitStrategy preSortByKey(SortOrder sortOrder) {
        this.modularStrategy = modularStrategy.withPostSortFunction(SortFunction.sortByKey(sortOrder));
        return this;
    }

    public DynamicSplitStrategy flatten() {
        this.modularStrategy = modularStrategy.withFlatten(true);
        return this;
    }
}
