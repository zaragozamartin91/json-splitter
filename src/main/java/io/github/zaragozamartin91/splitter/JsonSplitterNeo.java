package io.github.zaragozamartin91.splitter;

import io.github.zaragozamartin91.splitter.flat.ExpandedJson;
import io.github.zaragozamartin91.splitter.flat.FlatJson;
import io.github.zaragozamartin91.splitter.flat.JsonFlattener;
import io.github.zaragozamartin91.splitter.flat.JsonUnflattener;
import io.github.zaragozamartin91.splitter.split.SplitByChunkSize;
import io.github.zaragozamartin91.splitter.split.SplitByEntryCount;
import io.github.zaragozamartin91.splitter.split.SplitByNumberOfParts;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JsonSplitterNeo implements Function<JsonSourceNeo, SplitJsonNeo> {
    private final JsonSplitterConfig config;
    private final JsonCodec jsonCodec;
    private final JsonFlattener jsonFlattener;
    private final JsonUnflattener jsonUnflattener;

    public JsonSplitterNeo(final JsonSplitterConfig config) {
        this.config = config.valid();
        jsonCodec = JsonCodec.instance();
        jsonFlattener = new JsonFlattener(jsonCodec);
        jsonUnflattener = new JsonUnflattener();
    }

    @Override
    public SplitJsonNeo apply(JsonSourceNeo jsonSource) {
        JsonCodec.JsonBox jsonBox = jsonCodec.polyReadTree(jsonSource.getContent());
        Map<String, Object> normalInput;
        /* If the input is a pure array then flattening is mandatory */
        boolean flatAndExpand = config.flatten() || jsonBox.isArray();
        if (flatAndExpand) {
            FlatJson flatJson = jsonFlattener.flatten(jsonCodec.toMap(jsonBox));
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

        return new SplitJsonNeo(jsonParts);
    }

    private List<JsonPart> expandedJsonParts(List<Map<String, Object>> result, boolean pureArrayContainer) {
        return result.stream().map(jsonUnflattener::unflatten)
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
        JsonUnflattener jsonUnflattener = new JsonUnflattener();
        ExpandedJson unflatten = jsonUnflattener.unflatten(flatMap);
        return unflatten.jsonMap();
    }
}
