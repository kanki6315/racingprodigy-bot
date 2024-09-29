package com.racingprodigy.standings_bot.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeAdapter implements JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public OffsetDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return OffsetDateTime.parse(jsonElement.getAsString(), formatter);
    }

    @Override
    public JsonPrimitive serialize(OffsetDateTime offsetDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(offsetDateTime.format(formatter));
    }
}
