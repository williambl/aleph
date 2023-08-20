package com.williambl.libs.aleph.httpclient;

import com.williambl.libs.aleph.failure.Failure;

import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Represents a failure arising from an unexpected status from an HTTP response.
 */
public record HttpStatusFailure<T>(String description, int expectedCode, int actualCode, HttpResponse<T> response) implements Failure {
    @Override
    public Optional<Failure> cause() {
        return Optional.empty();
    }

    @Override
    public Optional<Throwable> throwable() {
        return Optional.empty();
    }
}
