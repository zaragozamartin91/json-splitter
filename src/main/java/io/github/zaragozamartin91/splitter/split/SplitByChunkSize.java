package io.github.zaragozamartin91.splitter.split;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.zaragozamartin91.splitter.split.Operation.*;
import static java.math.RoundingMode.HALF_UP;

public enum SplitByChunkSize {
    INSTANCE;

    /**
     * Splits an input map into chunks of approximate equal size in bytes
     * @param input Map to split into parts
     * @param minSizeBytes Min size in bytes
     * @param maxSizeBytes Max size in bytes
     * @param sizeFunction Function to calculate the size of the map in bytes
     * @return List of separated maps
     */
    public List<Map<String, Object>> splitByChunkSize(
            Map<String, Object> input,
            long minSizeBytes,
            long maxSizeBytes,
            Function<Object, Long> sizeFunction,
            Function<List<Map.Entry<String, Object>>, Map<String, Object>> mapCreator) {
        if (Objects.isNull(input)) throw new IllegalArgumentException("Cannot split a null map");

        SizeInterval sizeInterval = new SizeInterval(minSizeBytes, maxSizeBytes);

        if (input.isEmpty()) return List.of();

        if (input.size() == 1) return List.of(input);

        LinkedHashMap<String, Object> orderedInput = new LinkedHashMap<>(input);

        List<HeftyMap> heftyMaps = splitByChunkSize(
                orderedInput,
                sizeInterval,
                sizeFunction
        );

        return heftyMaps.stream().map(heftyMap -> heftyMap.toMap(mapCreator)).collect(Collectors.toList());
    }

    private List<HeftyMap> splitByChunkSize(
            LinkedHashMap<String, Object> input,
            SizeInterval sizeInterval,
            Function<Object, Long> sizeFunction
    ) {
        ArrayList<HeftyMap> accumulator = new ArrayList<>();

        Long inputSizeBytes = sizeFunction.apply(input);
        List<Map.Entry<String, Object>> inputEntryList = new ArrayList<>(input.entrySet());

        /* No need to do any splitting if the total size of the input is smaller than the requested chunk size */
        if (sizeInterval.fits(inputSizeBytes)) {
            accumulator.add(new HeftyMap(inputEntryList, inputSizeBytes));
            return accumulator;
        }

        int inputEntryCount = input.size();
        BigDecimal avgEntrySizeBytes = averageEntrySize(input, inputSizeBytes);
        Window leftWindow = new Window(0, divide(sizeInterval.mid(), avgEntrySizeBytes).intValue());
        Window rightWindow = leftWindow.shiftRight().resizeRight(inputEntryCount);

        HashMap<Window, HeftyMap> memoizedEntries = new HashMap<>();
        HashSet<Window> memoizedWindows = new HashSet<>();

        HeftyMap leftHeftyMap;
        HeftyMap rightHeftyMap;

        while (leftWindow.active()) {
            leftWindow = leftWindow.normalize();
            rightWindow = rightWindow.normalize();

            leftHeftyMap = memoize(memoizedEntries, leftWindow, inputEntryList, sizeFunction);
            rightHeftyMap = memoize(memoizedEntries, rightWindow, inputEntryList, sizeFunction);

            if (memoizedWindows.contains(leftWindow)) {
                /* This position has already been visited ; store the map as part of the final result */
                accumulator.add(leftHeftyMap);
                leftWindow = leftWindow.shiftRight().limit(inputEntryCount);
                rightWindow = rightWindow.shiftRight(leftWindow.right).resizeRight(inputEntryCount);
                continue;
            }

            memoizedWindows.add(leftWindow);

            if (sizeInterval.fits(leftHeftyMap.sizeBytes)) {
                accumulator.add(leftHeftyMap);
                leftWindow = leftWindow.shiftRight().limit(inputEntryCount);
                rightWindow = rightWindow.shiftRight(leftWindow.right).resizeRight(inputEntryCount);
            } else if (sizeInterval.tooSmall(leftHeftyMap.sizeBytes)) {
                /* current cut is too small */
                /* Need to expand the window on the right side by X units */
                BigDecimal rightMapAvgEntrySizeBytes = rightHeftyMap.averageEntrySizeBytes();
                BigDecimal diffToMidBytes = bigDecimal(sizeInterval.diffToMid(leftHeftyMap.sizeBytes));
                int units = steps(diffToMidBytes, rightMapAvgEntrySizeBytes);
                leftWindow = leftWindow.expandRight(units).limit(inputEntryCount);
                rightWindow = rightWindow.shiftRight(leftWindow.right).resizeRight(inputEntryCount);
            } else if (sizeInterval.tooBig(leftHeftyMap.sizeBytes)) {
                /* current cut is too large */
                /* need to shrink the window on the right side by X units */
                BigDecimal leftMapAvgEntrySizeBytes = leftHeftyMap.averageEntrySizeBytes();
                BigDecimal diffToMidBytes = bigDecimal(sizeInterval.diffToMid(leftHeftyMap.sizeBytes));
                int units = steps(diffToMidBytes, leftMapAvgEntrySizeBytes);
                leftWindow = leftWindow.contractRight(units);
                rightWindow = rightWindow.shiftRight(leftWindow.right).resizeRight(inputEntryCount);
            }
        }
        return accumulator;
    }

    /**
     * Compute steps to move the sliding window
     * @param diffBytes Distance in bytes to target
     * @param avgEntrySizeBytes Average entry size in bytes
     * @return Number of steps to move the sliding window
     */
    private static int steps(BigDecimal diffBytes, BigDecimal avgEntrySizeBytes) {
        if (BigDecimal.ZERO.equals(diffBytes) || BigDecimal.ZERO.equals(avgEntrySizeBytes)) return 0;
        return divide(diffBytes, avgEntrySizeBytes).intValue();
    }

    private static HeftyMap memoize(
            HashMap<Window, HeftyMap> memoizedMaps,
            Window window,
            List<Map.Entry<String, Object>> entryList,
            Function<Object, Long> sizeFunction
    ) {
        if (memoizedMaps.containsKey(window)) {
            return memoizedMaps.get(window);
        } else {
            List<Map.Entry<String, Object>> subEntries = window.subList(entryList);
            HeftyMap newHeftyMap = new HeftyMap(subEntries, sizeFunction.apply(subEntries));
            memoizedMaps.put(window, newHeftyMap);
            return newHeftyMap;
        }
    }

    public static long mapSizeAsBytes(Object obj, Function<Object, Integer> sizeInBytes) {
        return JsonSize.mapSizeAsBytes(obj, sizeInBytes);
    }


    public static long flatMapSizeAsBytes(Object obj,
                                   Function<Object, Integer> sizeInBytes,
                                   Function<Collection<Map.Entry<String, Object>>, Map<String, Object>> unflatten) {
        return JsonSize.flatMapSizeAsBytes(obj, sizeInBytes, unflatten);
    }
}


final class Window {
    final int left;
    final int right;

    public Window(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public int len() {
        return Math.max(0, right - left);
    }

    <T> List<T> subList(List<T> items) {
        return len() <= 0 ? List.of() : items.subList(left, right);
    }

    Window shiftRight() {
        return shiftRight(right);
    }

    /**
     * Moves the window to the right.
     * The same size is kept.
     * @param newLeftPosition New left position.
     * @return New window moved to the right by {@code positions}.
     */
    Window shiftRight(int newLeftPosition) {
        return new Window(newLeftPosition, newLeftPosition + len());
    }

    Window limit(int rightmost) {
        return new Window(left, Math.min(right, rightmost));
    }

    Window normalize() {
        int newLeft = Math.min(left, right);
        return new Window(newLeft, right);
    }

    Window expandRight(int units) {
        return resizeRight(right + units);
    }

    Window resizeRight(int rightmost) {
        return new Window(left, rightmost);
    }

    Window contractRight(int units) {
        return resizeRight(right - units);
    }

    boolean active() {
        return len() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Window window = (Window) o;
        return left == window.left && right == window.right;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return String.format("{left=%d, right=%d}", left, right);
    }
}

/**
 * Size interval in bytes
 */
final class SizeInterval {
    long minSize;
    long maxSize;

    public SizeInterval(long minSize, long maxSize) {
        if (minSize <= 0) throw new IllegalArgumentException("minSize must be higher than 0");
        if (maxSize <= 0) throw new IllegalArgumentException("maxSize must be higher than 0");
        if (maxSize < minSize) throw new IllegalArgumentException("minSize must be smaller than maxSize");

        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    boolean fits(Number otherSize) {
        long l = otherSize.longValue();
        return minSize <= l && l <= maxSize;
    }

    /**
     * True if the incoming value is left from the interval (too small)
     */
    boolean tooSmall(Number value) {
        return value.longValue() < minSize;
    }

    /**
     * Trie if the incoming value is right from the interval (too big)
     */
    boolean tooBig(Number value) {
        return value.longValue() > maxSize;
    }

    long diff() {
        return maxSize - minSize;
    }

    long mid() {
        BigDecimal halfWay = divide(diff(), 2);
        return halfWay.add(bigDecimal(minSize)).longValue();
    }

    long diffToMid(long value) {
        long diff = mid() - value;
        return Math.abs(diff);
    }

    @Override
    public String toString() {
        return String.format("{minSize=%d, maxSize=%d}", minSize, maxSize);
    }
}

/**
 * Map with weight in bytes
 */
final class HeftyMap {
    List<Map.Entry<String, Object>> entries;
    long sizeBytes;

    public HeftyMap(List<Map.Entry<String, Object>> entries, long sizeBytes) {
        this.entries = entries;
        this.sizeBytes = sizeBytes;
    }

    BigDecimal averageEntrySizeBytes() {
        if (Objects.isNull(entries) || entries.isEmpty() || sizeBytes <= 0) return BigDecimal.ZERO;
        return divide(this.sizeBytes, this.entries.size());
    }

    Map<String, Object> toMap(Function<List<Map.Entry<String, Object>>, Map<String, Object>> mapCreator) {
        return mapCreator.apply(entries);
    }

    @Override
    public String toString() {
        return String.format("{sizeBytes=%d,entries=%s}", sizeBytes, entries);
    }
}

final class Operation {
    static BigDecimal averageEntrySize(Map<String, Object> input, Number inputSizeBytes) {
        BigDecimal inputEntryCount = bigDecimal(input.size());
        BigDecimal totalSizeBd = bigDecimal(inputSizeBytes);
        return divide(totalSizeBd, inputEntryCount);
    }

    static BigDecimal averageEntrySize(BigDecimal inputEntryCount, BigDecimal totalSizeBd) {
        return divide(totalSizeBd, inputEntryCount);
    }

    static BigDecimal averageEntrySize(Number entryCount, Number sizeBytes) {
        return averageEntrySize(bigDecimal(entryCount), bigDecimal(sizeBytes));
    }

    static BigDecimal bigDecimal(Number totalSize) {
        return BigDecimal.valueOf(totalSize.longValue()).setScale(4, HALF_UP);
    }

    static BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
        return numerator.divide(denominator, HALF_UP);
    }

    static BigDecimal divide(Number numerator, Number denominator) {
        return divide(bigDecimal(numerator), bigDecimal(denominator));
    }
}

final class JsonSize {
    static long mapSizeAsBytes(Object obj, Function<Object, Integer> sizeInBytes) {
        Map<String, Object> theMap = obj instanceof Map
                ? (Map<String, Object>) obj
                : newLinkedHashMap((List<Map.Entry<String, Object>>) obj);
        return sizeInBytes.apply(theMap);
    }

    static LinkedHashMap<String, Object> newLinkedHashMap(Collection<Map.Entry<String, Object>> entries) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : entries) map.put(entry.getKey(), entry.getValue());
        return map;
    }

    static long flatMapSizeAsBytes(Object obj,
                                    Function<Object, Integer> sizeInBytes,
                                    Function<Collection<Map.Entry<String, Object>>, Map<String, Object>> unflatten) {
        // a flat map
        if (obj instanceof Map) {
            Map<String, Object> flatMap = (Map<String, Object>) obj;
            Map<String, Object> unflattened = unflatten.apply(flatMap.entrySet());
            return mapSizeAsBytes(unflattened, sizeInBytes);
        }

        // an entry list
        if (obj instanceof List) {
            List<Map.Entry<String, Object>> entries = (List<Map.Entry<String, Object>>) obj;
            Map<String, Object> unflattened = unflatten.apply(entries);
            return mapSizeAsBytes(unflattened, sizeInBytes);
        }

        throw new IllegalArgumentException("Can't apply flatMapSizeAsBytes on " + obj);
    }
}
