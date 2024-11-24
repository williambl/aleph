package com.williambl.libs.aleph.uuid;

import com.williambl.libs.aleph.either.Either;
import com.williambl.libs.aleph.failure.Result;
import com.williambl.libs.aleph.json.JsonParseFailure;

import java.util.Optional;
import java.util.UUID;

/**
 * Utils for working with UUIDs.
 */
public class AUuid {
    /**
     * Try to parse the given string as a UUID.
     *
     * @param str the string to parse as a UUID
     * @return either a parsed UUID, or an empty optional
     */
    public static Optional<UUID> maybeMakeUuid(String str) {
        try {
            return Optional.of(UUID.fromString(str));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Try to parse the given string as a UUID, with failures represented as {@link UuidParseFailure}.
     *
     * @param str the string to parse as a UUID
     * @return either a parsed UUID, or a failure
     */
    public static Result<UUID> tryMakeUuid(String str) {
        try {
            return Result.ok(UUID.fromString(str));
        } catch (IllegalArgumentException e) {
            return Result.err(new UuidParseFailure("Failure parsing UUID String \"%s\": %s".formatted(str, e.getMessage()), Optional.of(e), Optional.of(str)));
        }
    }
}
