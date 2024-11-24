package com.williambl.libs.aleph.failure;

import com.williambl.libs.aleph.either.Either;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Like {@link Either}, but the Right side is always {@link Failure}.
 *
 * @param <E> the type of the value
 */
public sealed interface Result<E> {
    /**
     * Creates a value Result with the given value.
     * @param value the value
     * @return      the Result
     */
    static <L> Result<L> ok(L value) {
        return new Ok<>(value);
    }

    /**
     * Creates a failure Result with the given value.
     * @param value the value
     * @return      the Result
     */
    static <L> Result<L> err(Failure value) {
        return new Err<>(value);
    }

    /**
     * Creates an Result from the value if it exists, or else the failure value.
     * @param value  the value, or null
     * @param failure the failure value
     * @return      the Result
     */
    static <L> Result<L> of(@Nullable L value, Failure failure) {
        return value == null ? err(failure) : ok(value);
    }

    /**
     * Creates an Result from the contents of the value optional if present, or else get the failure value.
     * @param valueOpt  the value optional
     * @param failureSup the failure value supplier
     * @return         the Result
     */
    static <L> Result<L> of(Optional<L> valueOpt, Supplier<Failure> failureSup) {
        return valueOpt.map(Result::ok).orElseGet(() -> Result.err(failureSup.get()));
    }

    /**
     * Creates an Result from the contents of the failure optional if present, or else get the value.
     * @param valueSup  the value supplier
     * @param failureOpt the failure value optional
     * @return         the Result
     */
    static <L> Result<L> of(Supplier<L> valueSup, Optional<Failure> failureOpt) {
        return failureOpt.<Result<L>>map(Result::err).orElseGet(() -> Result.ok(valueSup.get()));
    }

    /**
     * If {@code toVerif} holds a value, and {@code possibleErr} outputs a non-empty Optional for that value,
     * returns a failure Result of that output. Otherwise, returns an Result equal to the original Result.
     * @param toVerif       the Result to verify the value side of
     * @param possibleErr   function that returns an optional failure error for the value
     * @return              a failure Result of the unwrapped output of {@code possibleErr}, or an Result equal to the input
     * @see #verifyList(Result, Function, Collector)
     */
    static <L> Result<L> verify(Result<L> toVerif, Function<L, Optional<Failure>> possibleErr) {
        return toVerif.then(l -> Result.of(() -> l, possibleErr.apply(l)));
    }

    /**
     * Given a list of Results, if all are value-valued, returns a value Result of the unwrapped values of those Results.
     * Otherwise, returns a failure Result of all the failure values found in the list, combined with the {@code errJoiner}
     * collector.
     * @param list      the list of eithers
     * @param errJoiner a {@link Collector} to collect failure values
     * @return          an Result of a list of values, or a combined failure value
     */
    static <L> Result<List<L>> bubbleErrorsUp(List<Result<L>> list, Collector<? super Failure, ?, Failure> errJoiner) {
        return fromEither(Either.bubbleErrorsUp(list.stream().map(Result::toEither).toList(), errJoiner));
    }

    static <L> Result<L> fromEither(Either<L, ? extends Failure> either) {
        return either.map(Result::ok, Result::err);
    }

    /**
     * If {@code toVerif} holds a value (a list), and {@code possibleErr} outputs a non-empty Optional for every value in the list,
     * returns a failure Result of the failure outputs, combined with {@code errJoiner}. Otherwise, returns an Result equal
     * to the original Result.
     * @param toVerif       the Result to verify the value side list of
     * @param possibleErr   function that returns an optional failure error for an element of the value side
     * @param errJoiner     a {@link Collector} to collect failure values
     * @return              a failure Result of a list of the unwrapped outputs of {@code possibleErr}, or an Result equal to the input
     * @see #verify(Result, Function)
     * @see #bubbleErrorsUp(List, Collector)
     */
    static <L, E1> Result<List<L>> verifyList(Result<List<Result<L>>> toVerif, Function<L, Optional<Failure>> possibleErr, Collector<? super Failure, ?, Failure> errJoiner) {
        return toVerif.flatMapBoth(ls ->
                bubbleErrorsUp(ls.stream().map(e -> Result.verify(e, possibleErr)).toList(), errJoiner),
                Result::err
        );
    }

    /**
     * Creates an Either holding this Result's contents.
     * @return  an Either
     */
    Either<E, Failure> toEither();

    /**
     * Maps both sides of the Result.
     * @param valueFunc  the function to map the value side
     * @param failureFunc the function to map the failure side
     * @return          the mapped Result
     */
    <E1> Result<E1> mapBoth(Function<E, E1> valueFunc, Function<Failure, Failure> failureFunc);

    /**
     * Maps the value side of the Result.
     * @param func  the function to map the value side
     * @return      the mapped Result
     */
    default <E1> Result<E1> map(Function<E, E1> func) {
        return this.mapBoth(func, Function.identity());
    }

    /**
     * Maps the failure side of the Result.
     * @param func  the function to map the failure side
     * @return      the mapped Result
     */
    default Result<E> mapErr(Function<Failure, Failure> func) {
        return this.mapBoth(Function.identity(), func);
    }

    /**
     * Flatmaps both sides of the Result.
     * @param valueFunc  the function to flatmap the value side
     * @param failureFunc the function to flatmap the failure side
     * @return          the flatmapped Result
     */
    <E1> Result<E1> flatMapBoth(Function<E, Result<E1>> valueFunc, Function<Failure, Result<E1>> failureFunc);

    /**
     * Flatmaps the value side of the Result.
     * @param func  the function to flatmap the value side
     * @return      the flatmapped Result
     */
    default <E1> Result<E1> then(Function<E, Result<E1>> func) {
        return this.flatMapBoth(func, Result::err);
    }

    /**
     * Flatmaps the value side of the Result.
     * @param func  the function to flatmap the value side
     * @return      the flatmapped Result
     */
    default <E1> Result<E1> flatMap(Function<E, Result<E1>> func) {
        return this.then(func);
    }

    /**
     * Flatmaps the failure side of the Result.
     * @param func  the function to flatmap the failure side
     * @return      the flatmapped Result
     */
    default Result<E> flatMapErr(Function<Failure, Result<E>> func) {
        return this.flatMapBoth(Result::ok, func);
    }

    /**
     * Maps the Result to a value of a single type.
     * @param valueFunc  the function to map the value side
     * @param failureFunc the function to map the failure side
     * @return          the mapped value
     */
    <T> T map(Function<E, T> valueFunc, Function<Failure, T> failureFunc);

    /**
     * Consumes either side of the Result.
     * @param valueFunc  the function to consume the value side
     * @param failureFunc the function to consume the failure side
     */
    void consume(Consumer<E> valueFunc, Consumer<Failure> failureFunc);

    /**
     * Whether this Result holds a value.
     * @return whether this Result holds a value
     */
    boolean isOk();

    /**
     * Whether this Result holds a failure value.
     * @return whether this Result holds a failure value
     */
    boolean isErr();

    /**
     * Gets the value of this Result.
     * @return the value
     * @throws NoSuchElementException if this Result does not hold a value
     */
    E value();

    /**
     * Gets the failure value of this Result.
     * @return the failure value
     * @throws NoSuchElementException if this Result does not hold a failure value
     */
    Failure err();

    /**
     * Gets the value of this Result wrapped in an Optional, or an empty Optional if there is no value.
     * @return the value wrapped in an Optional
     */
    Optional<E> maybeValue();

    /**
     * Gets the failure value of this Result wrapped in an Optional, or an empty Optional if there is no failure value.
     * @return the failure value wrapped in an Optional
     */
    Optional<Failure> maybeErr();

    record Ok<E>(E value) implements Result<E> {
        @Override
        public Either<E, Failure> toEither() {
            return Either.left(this.value);
        }

        @Override
        public <E1> Result<E1> mapBoth(Function<E, E1> valueFunc, Function<Failure, Failure> failureFunc) {
            return new Ok<>(valueFunc.apply(this.value));
        }

        @Override
        public <E1> Result<E1> flatMapBoth(Function<E, Result<E1>> valueFunc, Function<Failure, Result<E1>> failureFunc) {
            return valueFunc.apply(this.value);
        }

        @Override
        public <T> T map(Function<E, T> valueFunc, Function<Failure, T> failureFunc) {
            return valueFunc.apply(this.value);
        }

        @Override
        public void consume(Consumer<E> valueFunc, Consumer<Failure> failureFunc) {
            valueFunc.accept(this.value);
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public boolean isErr() {
            return false;
        }

        @Override
        public Failure err() {
            throw new NoSuchElementException("Tried to get failure value of a value Result");
        }

        @Override
        public Optional<E> maybeValue() {
            return Optional.of(this.value);
        }

        @Override
        public Optional<Failure> maybeErr() {
            return Optional.empty();
        }
    }

    record Err<L>(Failure err) implements Result<L> {
        @Override
        public Either<L, Failure> toEither() {
            return Either.right(this.err);
        }

        @Override
        public <E1> Result<E1> mapBoth(Function<L, E1> valueFunc, Function<Failure, Failure> failureFunc) {
            return new Err<>(failureFunc.apply(this.err));
        }

        @Override
        public <E1> Result<E1> flatMapBoth(Function<L, Result<E1>> valueFunc, Function<Failure, Result<E1>> failureFunc) {
            return failureFunc.apply(this.err);
        }

        @Override
        public <T> T map(Function<L, T> valueFunc, Function<Failure, T> failureFunc) {
            return failureFunc.apply(this.err);
        }

        @Override
        public void consume(Consumer<L> valueFunc, Consumer<Failure> failureFunc) {
            failureFunc.accept(this.err);
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public boolean isErr() {
            return true;
        }

        @Override
        public L value() {
            throw new NoSuchElementException("Tried to get value of a failure Result");
        }

        @Override
        public Optional<L> maybeValue() {
            return Optional.empty();
        }

        @Override
        public Optional<Failure> maybeErr() {
            return Optional.of(this.err);
        }
    }
}
