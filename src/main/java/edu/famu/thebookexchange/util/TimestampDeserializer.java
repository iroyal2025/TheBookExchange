package edu.famu.thebookexchange.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.cloud.Timestamp;

import java.io.IOException;

public class TimestampDeserializer extends JsonDeserializer<Timestamp> {
    @Override
    public Timestamp deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        // Parse the JSON into a JsonNode, which has the asLong and asInt methods
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        // Check if "seconds" and "nanos" are present in the node
        long seconds = node.has("seconds") ? node.get("seconds").asLong() : 0L;
        int nanos = node.has("nanos") ? node.get("nanos").asInt() : 0;

        return Timestamp.ofTimeSecondsAndNanos(seconds, nanos);
    }
}

