package io.github.zaragozamartin91.splitter;

public class GroupBySizeContext {
    private final long sizeInBytes;

    public GroupBySizeContext(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }
}
