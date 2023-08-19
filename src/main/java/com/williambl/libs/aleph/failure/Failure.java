package com.williambl.libs.aleph.failure;

import java.util.Optional;

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
}
