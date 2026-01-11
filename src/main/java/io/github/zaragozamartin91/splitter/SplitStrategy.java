package io.github.zaragozamartin91.splitter;

import java.util.List;
import java.util.Map;

/** Specifies a strategy to split a flattened json */
public interface SplitStrategy {
    public List<Map<String, Object>> split(JsonUnit jsonUnit);
}
