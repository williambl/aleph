package com.williambl.libs.aleph.httpclient;

import com.williambl.libs.aleph.either.Either;
import com.williambl.libs.aleph.failure.Result;
import com.williambl.libs.aleph.uuid.UuidParseFailure;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.security.URIParameter;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Utils for working with {@link HttpClient}.
 */
public class AHttp {
    /**
     * Verifies that an {@link HttpResponse} has an acceptable correct status. If it does not, an Either containing an {@link HttpStatusFailure} is returned.
     *
     * @param response         the response
     * @param predicate        the function determining if the response code is okay
     * @param expectedCodeName the name of the expected code used in the error message
     * @return an Either of the response with correct status code, or a failure
     */
    public static <T> Result<HttpResponse<T>> expectStatus(HttpResponse<T> response, Predicate<Integer> predicate, String expectedCodeName) {
        return predicate.test(response.statusCode()) ? Result.ok(response) : Result.err(new HttpStatusFailure<T>("Expected status %s from HTTP response, got status %s.".formatted(expectedCodeName, response.statusCode()), expectedCodeName, response.statusCode(), response));
    }

    /**
     * Convenience overload for {@link #expectStatus(HttpResponse, Predicate, String)} for a single status.
     */
    public static <T> Result<HttpResponse<T>> expectStatus(HttpResponse<T> response, int status) {
        return expectStatus(response, i -> i == status, Integer.toString(status));
    }

    /**
     * Convenience overload for {@link #expectStatus(HttpResponse, Predicate, String)} with 2xx as the expected status.
     */
    public static <T> Result<HttpResponse<T>> expectOk(HttpResponse<T> response) {
        return expectStatus(response, i -> i / 100 == 2, "2xx");
    }

    /**
     * Try to parse the given string as a URI, with failures represented as {@link UriParseFailure}.
     *
     * @param str the string to parse as a URI
     * @return either a parsed URI, or a failure
     */
    public static Result<URI> tryMakeUri(String str) {
        try {
            return Result.ok(new URI(str));
        } catch (URISyntaxException e) {
            return Result.err(new UriParseFailure("Failure parsing URI String \"%s\": %s".formatted(str, e.getMessage()), e, str));
        }
    }
}
