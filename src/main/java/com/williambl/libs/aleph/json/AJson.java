package com.williambl.libs.aleph.json;

import com.google.gson.*;
import com.williambl.libs.aleph.either.Either;
import com.williambl.libs.aleph.failure.Failure;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * More ergonomic JSON representations.
 */
public sealed interface AJson {
    /**
     * Convert a GSON JsonElement into an AJson.
     * @param element   the GSON JsonElement
     * @return          the AJson representation
     * @throws IllegalStateException    if the JsonElement is not a primitive, null, object, or array. If this throws, it's a bug!
     */
    static AJson fromGson(JsonElement element) {
        //TODO replace with pattern-matched switch in java 21
        if (element instanceof JsonNull || element == null) {
            return new AJsonNull();
        } else if (element instanceof JsonPrimitive prim) {
            if (prim.isString()) {
                return new AJsonString(prim.getAsString());
            } else if (prim.isNumber()) {
                return new AJsonNumber(prim.getAsDouble());
            } else if (prim.isBoolean()) {
                return new AJsonBoolean(prim.getAsBoolean());
            }
        } else if (element instanceof JsonArray arr) {
            return new AJsonArray(arr.asList().stream().map(AJson::fromGson).toList());
        } else if (element instanceof JsonObject obj) {
            return new AJsonObject(obj.asMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, kv -> AJson.fromGson(kv.getValue()))));
        }

        throw new IllegalStateException("Gson element %s was not a string, number, boolean, null, array, or object".formatted(element));
    }

    /**
     * Convert an AJson into a GSON JsonElement.
     * @param json  the AJSon
     * @return      the GSON representation
     */
    static JsonElement toGson(AJson json) {
        //TODO replace with pattern-matched switch in java 21
        if (Objects.requireNonNull(json) instanceof AJsonString str) {
            return new JsonPrimitive(str.value());
        } else if (json instanceof AJsonNumber num) {
            return new JsonPrimitive(num.value());
        } else if (json instanceof AJsonBoolean bool) {
            return new JsonPrimitive(bool.value());
        } else if (json instanceof AJsonNull) {
            return JsonNull.INSTANCE;
        } else if (json instanceof AJsonArray arr) {
            var jsonArray = new JsonArray();
            for (var element : arr.values()) {
                jsonArray.add(toGson(element));
            }
            return jsonArray;
        } else if (json instanceof AJsonObject obj) {
            var jsonObj = new JsonObject();
            for (var prop : obj.properties().entrySet()) {
                jsonObj.add(prop.getKey(), toGson(prop.getValue()));
            }
            return jsonObj;
        }

        throw new IncompatibleClassChangeError("An AJson must be a string, number, boolean, null, array, or object, but it was %s".formatted(json.getClass()));
    }

    /**
     * Try to parse the given string as an AJson, with failures represented as {@link JsonParseFailure}.
     * JSON parsing is done with GSON in lenient mode.
     * @param str   the string to parse as JSON
     * @return      either a parsed JSON, or a failure.
     */
    static Either<AJson, JsonParseFailure> parse(String str) {
        try {
            return Either.left(AJson.fromGson(JsonParser.parseString(str)));
        } catch (JsonParseException e) {
            return Either.right(new JsonParseFailure(e.getMessage(), Optional.of(e), Optional.of(str)));
        }
    }

    static String name(Class<? extends AJson> clazz) {
        if (clazz == AJsonString.class) {
            return "String";
        } else if (clazz == AJsonNumber.class) {
            return "Number";
        } else if (clazz == AJsonBoolean.class) {
            return "Boolean";
        } else if (clazz == AJsonNull.class) {
            return "Null";
        } else if (clazz == AJsonArray.class) {
            return "Array";
        } else if (clazz == AJsonObject.class) {
            return "Object";
        } else {
            return "JSON";
        }
    }

    //TODO write

    record AJsonString(String value) implements AJson {}
    record AJsonNumber(double value) implements AJson {}
    record AJsonBoolean(boolean value) implements AJson {}
    record AJsonNull() implements AJson {}
    record AJsonArray(@Unmodifiable List<AJson> values) implements AJson {
        public AJsonArray(List<AJson> values) {
            this.values = List.copyOf(values);
        }

        public @Nullable AJson get(int index) {
            return index > 0 && index < this.values.size() ? this.values.get(index) : null;
        }

        public Optional<AJson> maybeGet(int index) {
            return Optional.ofNullable(this.get(index));
        }

        public Either<AJson, Failure> tryGet(int index) {
            return Either.of(this.maybeGet(index), () -> JsonGetFailure.noElement(index, this));
        }

        public <T extends AJson> Either<T, Failure> tryGet(int index, Class<T> clazz) {
            return this.tryGet(index).flatMapLeft(j ->
                    clazz.isInstance(j) ?
                            Either.left(clazz.cast(j)) :
                            Either.right(JsonGetFailure.wrongType(Integer.toString(index), clazz, j.getClass(), j)));
        }
    }
    record AJsonObject(@Unmodifiable Map<String, AJson> properties) implements AJson {
        public AJsonObject(Map<String, AJson> properties) {
            this.properties = Map.copyOf(properties);
        }

        public @Nullable AJson get(String key) {
            return this.properties.get(key);
        }

        public Optional<AJson> maybeGet(String key) {
            return Optional.ofNullable(this.properties.get(key));
        }

        public Either<AJson, Failure> tryGet(String key) {
            return Either.of(this.maybeGet(key), () -> JsonGetFailure.noProperty(key, this));
        }

        public <T extends AJson> Either<T, Failure> tryGet(String key, Class<T> clazz) {
            return this.tryGet(key).flatMapLeft(j ->
                    clazz.isInstance(j) ?
                            Either.left(clazz.cast(j)) :
                            Either.right(JsonGetFailure.wrongType(key, clazz, j.getClass(), j)));
        }
    }
}
