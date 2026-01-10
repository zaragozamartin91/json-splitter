package io.github.zaragozamartin91.splitter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SplitJson {
    private final List<FlatJson> parts;

    public SplitJson(Collection<FlatJson> parts) {
        this.parts = List.copyOf(parts);
    }

    public FlatJson getPart(int index) {
        return parts.get(index);
    }
}
