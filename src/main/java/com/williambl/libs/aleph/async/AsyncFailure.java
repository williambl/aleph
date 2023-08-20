package com.williambl.libs.aleph.async;

import com.williambl.libs.aleph.failure.Failure;

import java.util.Optional;

public record AsyncFailure(String description, Optional<Throwable> throwable) implements Failure {
    @Override
    public Optional<Failure> cause() {
        return Optional.empty();
    }
}
