package io.github.zaragozamartin91.splitter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class SplitBySizeStrategy extends ModularSplitStrategy {

    public SplitBySizeStrategy(
            boolean flatten,
            Function<Collection<Entry<String, Object>>, List<Entry<String, Object>>> preSortFunction,
            Function<List<Entry<String, Object>>, List<List<Entry<String, Object>>>> groupFunction,
            Function<List<List<Entry<String, Object>>>, List<Map<String, Object>>> collectFunction,
            Function<Collection<Entry<String, Object>>, List<Entry<String, Object>>> postSortFunction) {
        super(flatten, preSortFunction, groupFunction, collectFunction, postSortFunction);
    }

    public SplitBySizeStrategy(ModularSplitStrategy other) {
        super(other);
    }

    public SplitBySizeStrategy() {
        super();
    }

    @Override
    public SplitBySizeStrategy withFlatten(boolean flatten) {
        if (flatten) {
            throw new IllegalStateException("flatten=true is not supported by SplitBySizeStrategy");
        }
        return new SplitBySizeStrategy(this);
    }
}
