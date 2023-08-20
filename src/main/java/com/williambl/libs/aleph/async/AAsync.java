package com.williambl.libs.aleph.async;

import com.williambl.libs.aleph.either.Either;
import com.williambl.libs.aleph.failure.Failure;

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
    public static <LOut, ROut, LIn extends LOut, RIn extends ROut> CompletableFuture<Either<LOut, ROut>> unwrapFuture(
            Either<CompletableFuture<LIn>, RIn> eitherIn, Function<Exception, ROut> exceptionToFailure) {
        return CompletableFuture.supplyAsync(() -> eitherIn.<ROut>mapRight($ -> $).flatMapLeft(l -> {
            try {
                return Either.left(l.get());
            } catch (InterruptedException | ExecutionException e) {
                return Either.right(exceptionToFailure.apply(e));
            }
        }));
    }

    /**
     * Unwrap an Either of a CompletableFuture or a Failure, creating an {@link AsyncFailure} if there is an issue executing
     * asynchronously.
     * @param eitherIn  the Either to unwrap
     * @return          a CompletableFuture of the unwrapped Either
     */
    public static <L> CompletableFuture<Either<L, Failure>> unwrapFuture(
            Either<CompletableFuture<L>, ? extends Failure> eitherIn) {
        return AAsync.unwrapFuture(eitherIn, e -> new AsyncFailure(e.getMessage(), Optional.of(e)));
    }
}
