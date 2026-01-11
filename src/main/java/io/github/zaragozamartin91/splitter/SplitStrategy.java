package io.github.zaragozamartin91.splitter;

/** Specifies a strategy to split a flattened json */
public interface SplitStrategy {
    public SplitJson split(JsonUnit jsonUnit);
}
