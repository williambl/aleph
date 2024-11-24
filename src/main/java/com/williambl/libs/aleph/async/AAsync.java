package com.williambl.libs.aleph.async;

import com.williambl.libs.aleph.either.Either;
import com.williambl.libs.aleph.failure.Failure;
import com.williambl.libs.aleph.failure.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Utils for working with {@link CompletableFuture CompletableFutures} when using {@link Either} and {@link Failure}.
 */
public class AAsync {
    /**
     * From an Either of a CompletableFuture or another value, create a CompletableFuture of either one value or another.
     * @param eitherIn              the Either with the CompletableFuture on the left
     * @param exceptionToFailure    a function to turn exceptions into right-values for the Either
     * @return                      a CompletableFuture of an Either
     */
    public static <LOut, LIn extends LOut> CompletableFuture<Result<LOut>> unwrapFuture(
            Result<CompletableFuture<LIn>> eitherIn, Function<Exception, Failure> exceptionToFailure) {
        return CompletableFuture.supplyAsync(() -> eitherIn.<Failure>mapErr($ -> $).then(l -> {
            try {
                return Result.ok(l.get());
            } catch (InterruptedException | ExecutionException e) {
                return Result.err(exceptionToFailure.apply(e));
            }
        }));
    }

    /**
     * Unwrap an Either of a CompletableFuture or a Failure, creating an {@link AsyncFailure} if there is an issue executing
     * asynchronously.
     * @param eitherIn  the Either to unwrap
     * @return          a CompletableFuture of the unwrapped Either
     */
    public static <L> CompletableFuture<Result<L>> unwrapFuture(
            Result<CompletableFuture<L>> eitherIn) {
        return AAsync.unwrapFuture(eitherIn, e -> new AsyncFailure(e.getMessage(), Optional.of(e)));
    }

    /**
     * Performs an operation analagous to an {@link Either#flatMapLeft(Function)} on a CompletableFuture of an Either.
     * @param either    a CompletableFuture of an Either
     * @param func      the function to map the Either's left side to a CompletableFuture of a new Either
     * @return          a CompletableFuture of the flatmapped Either
     */
    public static <LIn, LOut> CompletableFuture<Result<LOut>> flatMapLeftAsync(CompletableFuture<Result<LIn>> either, Function<LIn, CompletableFuture<Result<LOut>>> func) {
        return either.thenCompose(e -> unwrapFuture(e.map(func)))
                .thenApply(e -> e.then($ -> $));
    }
}
