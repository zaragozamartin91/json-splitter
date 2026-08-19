package io.github.zaragozamartin91.splitter;

import java.nio.charset.Charset;

/**
 * Utility for serializing {@link JsonPart} objects back to text or bytes.
 */
public class JsonPartWriter {
    private final JsonCodec jsonCodec;

    /**
     * Creates a new JsonPartWriter instance using the default JSON codec.
     */
    public JsonPartWriter() {
        this(JsonCodec.instance());
    }

    JsonPartWriter(JsonCodec jsonCodec) {
        this.jsonCodec = jsonCodec;
    }

    /**
     * Serializes a JSON part to a string.
     * @param jsonPart The JSON part to serialize
     * @param charset The charset to use for encoding
     * @return The serialized JSON string
     */
    public String writeText(JsonPart jsonPart, Charset charset) {
        return jsonCodec.writeValueAsText(jsonPart.content(), charset);
    }

    /**
     * Serializes a JSON part to a byte array.
     * @param jsonPart The JSON part to serialize
     * @return The serialized JSON bytes
     */
    public byte[] writeBytes(JsonPart jsonPart) {
        return jsonCodec.writeValueAsBytes(jsonPart.content());
    }
}
