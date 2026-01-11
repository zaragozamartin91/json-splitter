package io.github.zaragozamartin91.splitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SplitJson {
    private final List<FlatJson> parts;

    public SplitJson(Collection<FlatJson> parts) {
        this.parts = new ArrayList<>(parts);
    }

    public FlatJson getPart(int index) {
        return parts.get(index);
    }

    public List<FlatJson> getParts() {
        return parts;
    }
}
