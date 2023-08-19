package com.williambl.libs.aleph.failure;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;

public interface Failure {
    /**
     * {@return the description of this failure}
     */
    String description();

    /**
     * {@return another failure which was the cause of this one}
     */
    Optional<Failure> cause();

    /**
     * Returns an optional of a throwable associated with this failure. This can provide stacktrace information.
     * @return a throwable associated with this failure
     */
    Optional<Throwable> throwable();

    static Failure create(String description) {
        return new GenericFailure(description, Optional.empty(), Optional.empty());
    }

    static Failure create(String description, Failure cause) {
        return new GenericFailure(description, Optional.of(cause), Optional.empty());
    }

    static Failure create(String description, Throwable throwable) {
        return new GenericFailure(description, Optional.empty(), Optional.of(throwable));
    }

    static Failure create(String description, Failure cause, Throwable throwable) {
        return new GenericFailure(description, Optional.of(cause), Optional.of(throwable));
    }

    static <T extends Failure> Collector<T, ?, MultiFailure> collector() {
        return new MultiFailure.FailureCollector<>();
    }

    static <T extends Failure> Collector<T, ?, MultiFailure> collector(Function<List<Failure>, String> descriptionMaker) {
        return new MultiFailure.FailureCollector<>(descriptionMaker);
    }
}
