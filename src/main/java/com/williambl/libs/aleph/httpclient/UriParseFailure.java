package com.williambl.libs.aleph.httpclient;

import com.williambl.libs.aleph.failure.Failure;

import java.net.URISyntaxException;
import java.util.Optional;

/**
 * Represents a failure parsing a URI string.
 */
public record UriParseFailure(String description, URISyntaxException exception, String input) implements Failure {
    @Override
    public Optional<Failure> cause() {
        return Optional.empty();
    }

    @Override
    public Optional<Throwable> throwable() {
        return Optional.of(this.exception());
    }
}
