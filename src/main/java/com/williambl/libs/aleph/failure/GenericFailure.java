package com.williambl.libs.aleph.failure;

import java.util.Optional;

public record GenericFailure(String description, Optional<Failure> cause, Optional<Throwable> throwable) implements Failure {
}
