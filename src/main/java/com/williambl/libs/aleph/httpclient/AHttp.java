package com.williambl.libs.aleph.httpclient;

import com.williambl.libs.aleph.either.Either;
import com.williambl.libs.aleph.uuid.UuidParseFailure;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.security.URIParameter;
import java.util.Optional;
import java.util.UUID;

/**
 * Utils for working with {@link java.net.http.HttpClient}.
 */
public class AHttp {
    /**
     * Verifies that an {@link HttpResponse} has the correct status. If it does not, an Either containing an {@link HttpStatusFailure} is returned.
     * @param response      the response
     * @param expectedCode  the expected status code
     * @return              an Either of the response with correct status code, or a failure
     */
    public static <T> Either<HttpResponse<T>, HttpStatusFailure<T>> expectStatus(HttpResponse<T> response, int expectedCode) {
        return response.statusCode() == expectedCode ? Either.left(response) : Either.right(new HttpStatusFailure<T>("Expected status %s from HTTP response, got status %s.", expectedCode, response.statusCode(), response));
    }

    /**
     * Convenience overload for {@link #expectStatus(HttpResponse, int)} with 200 OK as the expected status.
     */
    public static <T> Either<HttpResponse<T>, HttpStatusFailure<T>> expectOk(HttpResponse<T> response) {
        return expectStatus(response, 200);
    }

    /**
     * Try to parse the given string as a URI, with failures represented as {@link UriParseFailure}.
     * @param str   the string to parse as a URI
     * @return      either a parsed URI, or a failure
     */
    public static Either<URI, UriParseFailure> tryMakeUri(String str) {
        try {
            return Either.left(new URI(str));
        } catch (URISyntaxException e) {
            return Either.right(new UriParseFailure("Failure parsing URI String \"%s\": %s".formatted(str, e.getMessage()), e, str));
        }
    }
}
