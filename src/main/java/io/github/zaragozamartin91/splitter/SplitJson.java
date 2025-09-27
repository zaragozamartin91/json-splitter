package io.github.zaragozamartin91.splitter;

import java.util.Collection;

public class SplitJson {
    private final Collection<FlatJson> parts;

    public SplitJson(Collection<FlatJson> parts) {
        this.parts = parts;
    }
}
