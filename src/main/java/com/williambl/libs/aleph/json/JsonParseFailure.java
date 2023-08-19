package com.williambl.libs.aleph.json;

import com.google.gson.JsonElement;
import com.williambl.libs.aleph.failure.Failure;

import java.util.Optional;

/**
 * Represents a failure parsing JSON.
 */
public record JsonParseFailure(String description, Optional<Throwable> throwable, Optional<String> input) implements Failure {
    @Override
    public Optional<Failure> cause() {
        return Optional.empty();
    }
}
