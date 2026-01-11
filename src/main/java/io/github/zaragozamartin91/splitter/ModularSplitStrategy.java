package io.github.zaragozamartin91.splitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModularSplitStrategy implements SplitStrategy {
    private boolean flatten;
    private Function<Collection<Entry<String, Object>>, List<Entry<String, Object>>> preSortFunction;
    private Function<List<Entry<String, Object>>, List<List<Entry<String, Object>>>> groupFunction;
    private Function<List<List<Entry<String, Object>>>, List<Map<String, Object>>> collectFunction;
    private Function<Collection<Entry<String, Object>>, List<Entry<String, Object>>> postSortFunction;

    public ModularSplitStrategy(
            boolean flatten,
            Function<Collection<Entry<String, Object>>, List<Entry<String, Object>>> preSortFunction,
            Function<List<Entry<String, Object>>, List<List<Entry<String, Object>>>> groupFunction,
            Function<List<List<Entry<String, Object>>>, List<Map<String, Object>>> collectFunction,
            Function<Collection<Entry<String, Object>>, List<Entry<String, Object>>> postSortFunction) {
        this.flatten = flatten;
        this.preSortFunction = preSortFunction;
        this.groupFunction = groupFunction;
        this.collectFunction = collectFunction;
        this.postSortFunction = postSortFunction;
    }

    /* Copy constructor */
    public ModularSplitStrategy(ModularSplitStrategy other) {
        this(
                other.flatten,
                other.preSortFunction, // pre sort
                other.groupFunction, // group
                other.collectFunction, // collect
                other.postSortFunction // post sort
        );
    }

    public ModularSplitStrategy() {
        this(
                false,
                SortFunction::identity, // pre sort
                GroupFunction::identity, // group
                CollectFunction::collectToLinkedHashMap, // collect
                SortFunction::identity // post sort
        );
    }

    @Override
    public SplitJson split(JsonUnit jsonUnit) {
        if (Objects.isNull(jsonUnit)) {
            return new SplitJson(new ArrayList<>());
        }

        FlatJson flatJson = this.flatten ? jsonUnit.flatten() : jsonUnit.plain();

        Map<String, Object> flatJsonData = flatJson.getData();

        /* Entries are pre-sorted */
        Collection<Entry<String, Object>> entrySet = flatJsonData.entrySet();
        List<Entry<String, Object>> sortedEntries = this.preSort(entrySet);

        /* Entries are split into groups */
        List<List<Entry<String, Object>>> groups = group(sortedEntries);

        /* Each group is post-sorted */
        List<List<Entry<String, Object>>> sortedGroups = groups.stream()
                .map(this::postSort)
                .collect(Collectors.toList());

        List<Map<String, Object>> parts = collect(sortedGroups);
        List<FlatJson> flatJsons = parts.stream()
                .map(sd -> new FlatJson(sd))
                .collect(Collectors.toList());

        return new SplitJson(flatJsons);
    }

    private List<Entry<String, Object>> preSort(Collection<Entry<String, Object>> entrySet) {
        return preSortFunction.apply(entrySet);
    }

    private List<List<Entry<String, Object>>> group(List<Entry<String, Object>> sortedEntries) {
        return groupFunction.apply(sortedEntries);
    }

    private List<Map<String, Object>> collect(List<List<Entry<String, Object>>> sortedGroups) {
        return collectFunction.apply(sortedGroups);
    }

    private List<Entry<String, Object>> postSort(Collection<Entry<String, Object>> entrySet) {
        return postSortFunction.apply(entrySet);
    }

    /* Builder pattern ---- */

    private ModularSplitStrategy setFlatten(boolean flatten) {
        this.flatten = flatten;
        return this;
    }

    private ModularSplitStrategy setPreSortFunction(
            Function<Collection<Entry<String, Object>>, List<Entry<String, Object>>> preSortFunction) {
        this.preSortFunction = preSortFunction;
        return this;
    }

    private ModularSplitStrategy setGroupFunction(
            Function<List<Entry<String, Object>>, List<List<Entry<String, Object>>>> groupFunction) {
        this.groupFunction = groupFunction;
        return this;
    }

    private ModularSplitStrategy setCollectFunction(
            Function<List<List<Entry<String, Object>>>, List<Map<String, Object>>> collectFunction) {
        this.collectFunction = collectFunction;
        return this;
    }

    private ModularSplitStrategy setPostSortFunction(
            Function<Collection<Entry<String, Object>>, List<Entry<String, Object>>> postSortFunction) {
        this.postSortFunction = postSortFunction;
        return this;
    }

    /* withers */

    public ModularSplitStrategy withFlatten(boolean flatten) {
        return new ModularSplitStrategy(this).setFlatten(flatten);
    }

    public ModularSplitStrategy withPreSortFunction(
            Function<Collection<Entry<String, Object>>, List<Entry<String, Object>>> preSortFunction) {
        return new ModularSplitStrategy(this).setPreSortFunction(preSortFunction);
    }

    public ModularSplitStrategy withGroupFunction(
            Function<List<Entry<String, Object>>, List<List<Entry<String, Object>>>> groupFunction) {
        return new ModularSplitStrategy(this).setGroupFunction(groupFunction);
    }

    public ModularSplitStrategy withCollectFunction(
            Function<List<List<Entry<String, Object>>>, List<Map<String, Object>>> collectFunction) {
        return new ModularSplitStrategy(this).setCollectFunction(collectFunction);
    }

    public ModularSplitStrategy withPostSortFunction(
            Function<Collection<Entry<String, Object>>, List<Entry<String, Object>>> postSortFunction) {
        return new ModularSplitStrategy(this).setPostSortFunction(postSortFunction);
    }
}
