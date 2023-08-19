package com.williambl.libs.aleph.json;

import com.google.gson.JsonElement;
import com.williambl.libs.aleph.failure.Failure;

import java.util.Optional;

/**
 * Represents a failure retrieving an element from JSON.
 */
public sealed interface JsonGetFailure extends Failure {
    AJson json();

    record GetObjectPropertyFailure(String description, AJson.AJsonObject json) implements JsonGetFailure {
        @Override
        public Optional<Failure> cause() {
            return Optional.empty();
        }

        @Override
        public Optional<Throwable> throwable() {
            return Optional.empty();
        }
    }
    record GetArrayElementFailure(String description, AJson.AJsonArray json) implements JsonGetFailure {
        @Override
        public Optional<Failure> cause() {
            return Optional.empty();
        }

        @Override
        public Optional<Throwable> throwable() {
            return Optional.empty();
        }
    }
    record WrongJsonTypeFailure(String description, AJson json, Class<? extends AJson> expected, Class<? extends AJson> actual) implements JsonGetFailure {
        @Override
        public Optional<Failure> cause() {
            return Optional.empty();
        }

        @Override
        public Optional<Throwable> throwable() {
            return Optional.empty();
        }
    }
    static GetObjectPropertyFailure noProperty(String key, AJson.AJsonObject aJsonObject) {
        return new GetObjectPropertyFailure("No such property %s on object".formatted(key), aJsonObject);
    }

    static GetArrayElementFailure noElement(int index, AJson.AJsonArray aJsonArray) {
        return new GetArrayElementFailure("No such element %s in array".formatted(index), aJsonArray);
    }

    static <T extends AJson> WrongJsonTypeFailure wrongType(String key, Class<T> expected, Class<? extends AJson> actual, AJson json) {
        return new WrongJsonTypeFailure("Expected %s to be a %s, but was a %s".formatted(key, AJson.name(expected), AJson.name(actual)), json, expected, actual);
    }
}
