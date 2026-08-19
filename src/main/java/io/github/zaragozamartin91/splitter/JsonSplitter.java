package io.github.zaragozamartin91.splitter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Core engine responsible for executing the split operation based on a configuration.
 */
public class JsonSplitter implements Function<JsonSource, SplitJson> {
    private final JsonSplitterConfig config;
    private final JsonCodec jsonCodec;
    private final JsonFlattener jsonFlattener;
    private final JsonExpander jsonExpander;

    /**
     * Creates a new JsonSplitter instance with the specified configuration.
     * @param config The configuration to use for splitting
     * @throws IllegalStateException if the configuration is invalid
     */
    public JsonSplitter(final JsonSplitterConfig config) {
        this.config = config.valid();
        jsonCodec = JsonCodec.instance();
        jsonFlattener = new JsonFlattener(jsonCodec);
        jsonExpander = new JsonExpander();
    }

    /**
     * Splits the provided JSON source into multiple parts based on the configured strategy.
     * @param jsonSource The JSON source to split
     * @return A SplitJson instance containing the resulting parts
     */
    @Override
    public SplitJson apply(JsonSource jsonSource) {
        JsonCodec.JsonBox jsonBox = jsonCodec.polyReadTree(jsonSource.getContent());
        Map<String, Object> normalInput;
        /* If the input is a pure array then flattening is mandatory */
        boolean flatAndExpand = config.flatten() || jsonBox.isArray();
        if (flatAndExpand) {
            JsonPart flatJson = jsonFlattener.flatten(jsonCodec.toMap(jsonBox));
            normalInput = flatJson.jsonMap();
        } else {
            normalInput = jsonCodec.toMap(jsonBox);
        }

        List<Map<String, Object>> result;
        switch (config.strategy()) {
            case SPLIT_BY_CHUNK_SIZE:
                Function<Object, Long> sizeFunction = flatAndExpand ? this::flatMapSizeAsBytes : this::mapSizeAsBytes;
                result = SplitByChunkSize.INSTANCE.splitByChunkSize(
                        normalInput,
                        config.minSizeBytes(),
                        config.maxSizeBytes(),
                        sizeFunction,
                        this::newLinkedHashMap
                );
                break;
            case SPLIT_BY_ENTRY_COUNT:
                result = SplitByEntryCount.INSTANCE.splitByEntryCount(
                        normalInput, config.entryCount()
                );
                break;
            case SPLIT_BY_NUMBER_OF_PARTS:
                result= SplitByNumberOfParts.INSTANCE.splitByNumberOfParts(
                        normalInput, config.numberOfParts()
                );
                break;
            default: // Cannot happen
                result = null;
                break;
        }

        List<JsonPart> jsonParts;
        if (flatAndExpand) {
            boolean pureArrayContainer = jsonBox.isArray();
            jsonParts = expandedJsonParts(result, pureArrayContainer);
        } else {
            jsonParts = result.stream().map(JsonPart::map).collect(Collectors.toList());
        }

        return new SplitJson(jsonParts);
    }

    private List<JsonPart> expandedJsonParts(List<Map<String, Object>> result, boolean pureArrayContainer) {
        return result.stream().map(jsonExpander::unflatten)
                .map(expandedJson -> pureArrayContainer
                        ? JsonPart.array(expandedJson.jsonArray())
                        : JsonPart.map(expandedJson.jsonMap()))
                .collect(Collectors.toList());
    }

    private long mapSizeAsBytes(Object obj) {
        return SplitByChunkSize.mapSizeAsBytes(obj, jsonCodec::sizeInBytes);
    }

    private LinkedHashMap<String, Object> newLinkedHashMap(Collection<Map.Entry<String, Object>> entries) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : entries) map.put(entry.getKey(), entry.getValue());
        return map;
    }

    private long flatMapSizeAsBytes(Object obj) {
        return SplitByChunkSize.flatMapSizeAsBytes(obj, jsonCodec::sizeInBytes, this::unflattenAndMap);
    }

    private Map<String, Object> unflattenAndMap(Collection<Map.Entry<String, Object>> entries) {
        LinkedHashMap<String, Object> flatMap = newLinkedHashMap(entries);
        JsonExpander jsonExpander = new JsonExpander();
        JsonPart unflatten = jsonExpander.unflatten(flatMap);
        return unflatten.jsonMap();
    }
}
