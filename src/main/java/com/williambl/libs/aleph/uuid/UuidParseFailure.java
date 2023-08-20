package com.williambl.libs.aleph.uuid;

import com.williambl.libs.aleph.failure.Failure;

import java.util.Optional;

/**
 * Represents a failure parsing a UUID string.
 */
public record UuidParseFailure(String description, Optional<Throwable> throwable, Optional<String> input) implements Failure {
    @Override
    public Optional<Failure> cause() {
        return Optional.empty();
    }

}
