package com.williambl.libs.aleph.httpclient;

import com.williambl.libs.aleph.either.Either;

import java.net.http.HttpResponse;

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
}
