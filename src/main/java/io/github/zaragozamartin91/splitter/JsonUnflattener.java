package io.github.zaragozamartin91.splitter;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonUnflattener {

    /* Todo : unflatten may return a list | array instead of a map */
    public UnflattenedJson unflatten(Map<String, Object> theFlatMap) {
        if (theFlatMap == null || theFlatMap.isEmpty()) return new UnflattenedJson(theFlatMap);

//        boolean isPureArray = theFlatMap.entrySet().stream().anyMatch(entry -> {
//            HeadAndTail headAndTail = HeadAndTail.from(entry.getKey());
//            return isPureArrayKey(headAndTail.head);
//        });

        Map<String, Object> root = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : theFlatMap.entrySet()) {
            String path = entry.getKey();
            Object value = entry.getValue();
            unflatten(root, path, value);
        }

        return new UnflattenedJson(root);
    }

    @SuppressWarnings("unchecked")
    private void unflatten(Map<String, Object> root, String path, Object value) {
        if (path == null || path.isBlank()) return;

        if (isLeafPath(path)) {
            setLeafValue(root, path, value);
            return;
        }

        // otherwise is intermediate node ; eg:
        // * "foo.bar"
        // * "foo.bar.baz"
        HeadAndTail headAndTail = HeadAndTail.from(path);
        if (isNodeKey(headAndTail.head)) {
            Map<String, Object> intermediateNode = (Map<String, Object>) root.computeIfAbsent(headAndTail.head, key -> new HashMap<>());
            unflatten(intermediateNode, headAndTail.tailPath(), value);
            return;
        }
        
        // otherwise is an intermediate field array eg:
        // * foo[1].bar
        // * foo[1].baz[2]
        ArrayKeyAndIndex arrayKeyAndIndex = ArrayKeyAndIndex.from(headAndTail);
        if (arrayKeyAndIndex.isFieldArray()) {
            ArrayList<Object> theArray = (ArrayList<Object>) root.computeIfAbsent(arrayKeyAndIndex.key, key -> new ArrayList<>());
            ArrayKeyAndIndex arrayKeyAndIndexSansKey = arrayKeyAndIndex.consumeKey();
            HeadAndTail arrayHeadAndTailSansKey = new HeadAndTail(arrayKeyAndIndexSansKey.keyIndexText(), headAndTail.tail);
            setIntermediateArray(theArray, arrayKeyAndIndex.index, arrayHeadAndTailSansKey.fullPath(), value);
            return;
        }

        unflatten(root, headAndTail.tailPath(), value);
    }

    private void setIntermediateArray(ArrayList<Object> rootArray,
                                      int index,
                                      String path,
                                      Object value) {
        if (isLeafPath(path)) {
            setArrayValue(rootArray, index, value);
            return;
        }

        // otherwise is intermediate node
        HeadAndTail headAndTail = HeadAndTail.from(path);
        if (isNodeKey(headAndTail.head)) {
            Map<String, Object> intermediateNode = new HashMap<>();
            setArrayValue(rootArray, index, intermediateNode);
            unflatten(intermediateNode, headAndTail.tailPath(), value);
            return;
        }

        // otherwise is an intermediate array
        ArrayKeyAndIndex arrayKeyAndIndex = ArrayKeyAndIndex.from(headAndTail);
        if (arrayKeyAndIndex.isFieldArray()) {
            HashMap<String, Object> node = upsertArrayValue(rootArray, index, HashMap::new);
            unflatten(node, headAndTail.tailPath(), value);
            return;
        }

        ArrayList<Object> anotherArray = new ArrayList<>();
        setArrayValue(rootArray, index, anotherArray);
        setIntermediateArray(anotherArray, arrayKeyAndIndex.index, headAndTail.tailPath(), value);
    }

    @SuppressWarnings("unchecked")
    private void setLeafValue(Map<String, Object> root, String fieldName, Object value) {
        if (isNodeKey(fieldName)) {
            root.put(fieldName, value);
            return;
        }

        // otherwise is array
        ArrayKeyAndIndex arrayKeyAndIndex = ArrayKeyAndIndex.from(fieldName);
        ArrayList<Object> theArray = (ArrayList<Object>) root.computeIfAbsent(arrayKeyAndIndex.key, key -> new ArrayList<>());

        int index = arrayKeyAndIndex.index;
        setArrayValue(theArray, index, value);
    }

    private static void setArrayValue(ArrayList<Object> theArray, int index, Object value) {
        int expectedSize = index + 1;
        if (theArray.size() < expectedSize) theArray.add(value);
        else theArray.set(index, value);
    }

    /**
     * Attempts to get the array value by the index.
     * If the index is higher than the array capacity it inserts the value.
     * @param theArray Array to inspect
     * @param index Index of item to fetch
     * @param valueSupplier Supplier of value in case the index does not exist
     * @return Existing item or new item
     * @param <T> Type of the expected return value
     */
    @SuppressWarnings("unchecked")
    private static <T> T upsertArrayValue(ArrayList<Object> theArray, int index, Supplier<T> valueSupplier) {
        int expectedSize = index + 1;
        if (theArray.size() < expectedSize) {
            T value = valueSupplier.get();
            theArray.add(value);
            return value;
        } else return (T) theArray.get(index);
    }

    static boolean isArrayKey(String key) {
        return key.contains("[") && key.endsWith("]");
    }

    static boolean isNodeKey(String key) {
        return !isArrayKey(key);
    }

    static boolean isPureArrayKey(String key) {
        return key.startsWith("[") && key.endsWith("]");
    }

    static boolean isLeafPath(String path) {
        return !path.contains(".");
    }
    
    static final class HeadAndTail {
        final String head;
        final Deque<String> tail;

        HeadAndTail(String head, Deque<String> tail) {
            this.head = head;
            this.tail = tail;
        }

        String tailPath() {
            return String.join(".", tail);
        }

        static HeadAndTail from(String head, Deque<String> tail) {
            return new HeadAndTail(head, tail);
        }

        static HeadAndTail from(String path) {
            String[] subPaths = path.split(Pattern.quote("."));
            Deque<String> pathQueue = new ArrayDeque<>(Arrays.asList(subPaths));
            String head = pathQueue.pop();
            return from(head, pathQueue);
        }

        boolean isLeaf() {
            return tail.isEmpty();
        }

        String fullPath() {
            return Stream.concat(
                    Optional.ofNullable(head).stream(),
                    Optional.of(tailPath()).stream()
            ).filter(Predicate.not(String::isBlank)).collect(Collectors.joining("."));
        }
    }

    static final class ArrayKeyAndIndex {
        final String key;
        final int index;
        final Deque<String> tail;

        ArrayKeyAndIndex(String key, int index, Deque<String> tail) {
            this.key = key;
            this.index = index;
            this.tail = tail;
        }

        static ArrayKeyAndIndex from(String path) {
            Object[] keyIndexTuple = keyIndexTuple(path);
            return new ArrayKeyAndIndex(
                    (String) keyIndexTuple[0],
                    (Integer) keyIndexTuple[1],
                     null);
        }

        static ArrayKeyAndIndex from(HeadAndTail headAndTail) {
            Object[] keyIndexTuple = keyIndexTuple(headAndTail.head);
            return new ArrayKeyAndIndex(
                    (String) keyIndexTuple[0],
                    (Integer) keyIndexTuple[1],
                    headAndTail.tail
            );
        }

        private static Object[] keyIndexTuple(String path) {
            int indexOfLeft = path.indexOf('[');
            int indexOfRight = path.indexOf(']');
            String key = path.substring(0, indexOfLeft);
            String strIndex = path.substring(indexOfLeft + 1, indexOfRight);
            return new Object[] {key , Integer.parseInt(strIndex)};
        }

        /**
         * Yields true if this is an array with sub-fields
         * Example {@code "foobar[2].baz"}
         */
        boolean isFieldArray() {
            return Objects.nonNull(tail) && !tail.isEmpty();
        }

        /**
         * Consumes the key
         * Example: "foobar[2]" --> "[2]"
         */
        ArrayKeyAndIndex consumeKey() {
            return new ArrayKeyAndIndex("", index, this.tail);
        }

        /**
         * Yields a string representation of the pair
         * Example: {"foobar" , 2} --> "foobar[2]"
         */
        String keyIndexText() {
            return String.format("%s[%d]", key, index);
        }
    }
}
