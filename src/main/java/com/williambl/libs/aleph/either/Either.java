package com.williambl.libs.aleph.either;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.jetbrains.annotations.Nullable;

/**
 * A union - holds a value of either of two types, designated 'left' and 'right'. A little like {@link Optional},
 * but with two types (one of which will always be present).
 *
 * @param <L> the type of the left value
 * @param <R> the type of the right value
 */
public sealed interface Either<L, R> {
    /**
     * Creates a left Either with the given value.
     * @param value the value
     * @return      the Either
     */
    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    /**
     * Creates a right Either with the given value.
     * @param value the value
     * @return      the Either
     */
    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    /**
     * Creates an Either from the left value if it exists, or else the right value.
     * @param left  the left value, or null
     * @param right the right value
     * @return      the Either
     */
    static <L, R> Either<L, R> of(@Nullable L left, R right) {
        return left == null ? right(right) : left(left);
    }

    /**
     * Creates an Either from the contents of the left optional if present, or else get the right value.
     * @param leftOpt  the left value optional
     * @param rightSup the right value supplier
     * @return         the Either
     */
    static <L, R> Either<L, R> of(Optional<L> leftOpt, Supplier<R> rightSup) {
        return leftOpt.<Either<L, R>>map(Either::left).orElseGet(() -> Either.right(rightSup.get()));
    }

    /**
     * Creates an Either from the contents of the right optional if present, or else get the left value.
     * @param leftSup  the left value supplier
     * @param rightOpt the right value optional
     * @return         the Either
     */
    static <L, R> Either<L, R> of(Supplier<L> leftSup, Optional<R> rightOpt) {
        return rightOpt.<Either<L, R>>map(Either::right).orElseGet(() -> Either.left(leftSup.get()));
    }

    /**
     * If {@code toVerif} holds a left value, and {@code possibleErr} outputs a non-empty Optional for that value,
     * returns a right Either of that output. Otherwise, returns an Either equal to the original Either.
     * @param toVerif       the Either to verify the left side of
     * @param possibleErr   function that returns an optional right error for the left value
     * @return              a right Either of the unwrapped output of {@code possibleErr}, or an Either equal to the input
     * @see #verifyList(Either, Function, Collector)
     */
    static <L, R> Either<L, R> verify(Either<L, R> toVerif, Function<L, Optional<R>> possibleErr) {
        return toVerif.flatMapLeft(l -> Either.of(() -> l, possibleErr.apply(l)));
    }

    /**
     * Given a list of Eithers, if all are left-valued, returns a left Either of the unwrapped values of those Eithers.
     * Otherwise, returns a right Either of all the right values found in the list, combined with the {@code errJoiner}
     * collector.
     * @param list      the list of eithers
     * @param errJoiner a {@link Collector} to collect right values
     * @return          an Either of a list of left values, or a combined right value
     */
    static <L, R, R1> Either<List<L>, R> bubbleErrorsUp(List<Either<L, R1>> list, Collector<? super R1, ?, R> errJoiner) {
        return Either.<List<L>, List<R1>>right(list.stream().map(Either::maybeR).filter(Optional::isPresent).map(Optional::get).toList())
                .flatMapRight(errs -> errs.isEmpty() ? Either.left(list.stream().map(e -> e.left()).toList()) : Either.right(errs.stream().collect(errJoiner)));
    }

    /**
     * If {@code toVerif} holds a left value (a list), and {@code possibleErr} outputs a non-empty Optional for every value in the list,
     * returns a right Either of the right outputs, combined with {@code errJoiner}. Otherwise, returns an Either equal
     * to the original Either.
     * @param toVerif       the Either to verify the left side list of
     * @param possibleErr   function that returns an optional right error for an element of the left side
     * @param errJoiner     a {@link Collector} to collect right values
     * @return              a right Either of a list of the unwrapped outputs of {@code possibleErr}, or an Either equal to the input
     * @see #verify(Either, Function)
     * @see #bubbleErrorsUp(List, Collector)
     */
    static <L, R, R1> Either<List<L>, R> verifyList(Either<List<L>, R> toVerif, Function<L, Optional<R1>> possibleErr, Collector<? super R1, ?, R> errJoiner) {
        return toVerif.flatMapBoth(ls ->
                bubbleErrorsUp(ls.stream().map(Either::<L, R1>left).map(e -> Either.verify(e, possibleErr)).toList(), errJoiner),
                Either::right
        );
    }

    /**
     * Maps both sides of the Either.
     * @param leftFunc  the function to map the left side
     * @param rightFunc the function to map the right side
     * @return          the mapped Either
     */
    <L1, R1> Either<L1, R1> mapBoth(Function<L, L1> leftFunc, Function<R, R1> rightFunc);

    /**
     * Maps the left side of the Either.
     * @param func  the function to map the left side
     * @return      the mapped Either
     */
    default <L1> Either<L1, R> mapLeft(Function<L, L1> func) {
        return this.mapBoth(func, Function.identity());
    }

    /**
     * Maps the right side of the Either.
     * @param func  the function to map the right side
     * @return      the mapped Either
     */
    default <R1> Either<L, R1> mapRight(Function<R, R1> func) {
        return this.mapBoth(Function.identity(), func);
    }

    /**
     * Flatmaps both sides of the Either.
     * @param leftFunc  the function to flatmap the left side
     * @param rightFunc the function to flatmap the right side
     * @return          the flatmapped Either
     */
    <L1, R1> Either<L1, R1> flatMapBoth(Function<L, Either<L1, R1>> leftFunc, Function<R, Either<L1, R1>> rightFunc);

    /**
     * Flatmaps the left side of the Either.
     * @param func  the function to flatmap the left side
     * @return      the flatmapped Either
     */
    default <L1> Either<L1, R> flatMapLeft(Function<L, Either<L1, R>> func) {
        return this.flatMapBoth(func, Either::right);
    }

    /**
     * Flatmaps the right side of the Either.
     * @param func  the function to flatmap the right side
     * @return      the flatmapped Either
     */
    default <R1> Either<L, R1> flatMapRight(Function<R, Either<L, R1>> func) {
        return this.flatMapBoth(Either::left, func);
    }

    /**
     * Maps the Either to a value of a single type.
     * @param leftFunc  the function to map the left side
     * @param rightFunc the function to map the right side
     * @return          the mapped value
     */
    <T> T map(Function<L, T> leftFunc, Function<R, T> rightFunc);

    /**
     * Consumes either side of the Either.
     * @param leftFunc  the function to consume the left side
     * @param rightFunc the function to consume the right side
     */
    void consume(Consumer<L> leftFunc, Consumer<R> rightFunc);

    /**
     * Whether this Either holds a left value.
     * @return whether this Either holds a left value
     */
    boolean isLeft();

    /**
     * Whether this Either holds a right value.
     * @return whether this Either holds a right value
     */
    boolean isRight();

    /**
     * Gets the left value of this Either.
     * @return the left value
     * @throws NoSuchElementException if this Either does not hold a left value
     */
    L left();

    /**
     * Gets the right value of this Either.
     * @return the right value
     * @throws NoSuchElementException if this Either does not hold a right value
     */
    R right();

    /**
     * Gets the left value of this Either wrapped in an Optional, or an empty Optional if there is no left value.
     * @return the left value wrapped in an Optional
     */
    Optional<L> maybeL();

    /**
     * Gets the right value of this Either wrapped in an Optional, or an empty Optional if there is no right value.
     * @return the right value wrapped in an Optional
     */
    Optional<R> maybeR();

    record Left<L, R>(L value) implements Either<L, R> {
        @Override
        public <L1, R1> Either<L1, R1> mapBoth(Function<L, L1> leftFunc, Function<R, R1> rightFunc) {
            return new Left<>(leftFunc.apply(this.value));
        }

        @Override
        public <L1, R1> Either<L1, R1> flatMapBoth(Function<L, Either<L1, R1>> leftFunc, Function<R, Either<L1, R1>> rightFunc) {
            return leftFunc.apply(this.value);
        }

        @Override
        public <T> T map(Function<L, T> leftFunc, Function<R, T> rightFunc) {
            return leftFunc.apply(this.value);
        }

        @Override
        public void consume(Consumer<L> leftFunc, Consumer<R> rightFunc) {
            leftFunc.accept(this.value);
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public L left() {
            return this.value;
        }

        @Override
        public R right() {
            throw new NoSuchElementException("Tried to get right value of a left Either");
        }

        @Override
        public Optional<L> maybeL() {
            return Optional.of(this.value);
        }

        @Override
        public Optional<R> maybeR() {
            return Optional.empty();
        }
    }

    record Right<L, R>(R value) implements Either<L, R> {
        @Override
        public <L1, R1> Either<L1, R1> mapBoth(Function<L, L1> leftFunc, Function<R, R1> rightFunc) {
            return new Right<>(rightFunc.apply(this.value));
        }

        @Override
        public <L1, R1> Either<L1, R1> flatMapBoth(Function<L, Either<L1, R1>> leftFunc, Function<R, Either<L1, R1>> rightFunc) {
            return rightFunc.apply(this.value);
        }

        @Override
        public <T> T map(Function<L, T> leftFunc, Function<R, T> rightFunc) {
            return rightFunc.apply(this.value);
        }

        @Override
        public void consume(Consumer<L> leftFunc, Consumer<R> rightFunc) {
            rightFunc.accept(this.value);
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public L left() {
            throw new NoSuchElementException("Tried to get left value of a right Either");
        }

        @Override
        public R right() {
            return this.value;
        }

        @Override
        public Optional<L> maybeL() {
            return Optional.empty();
        }

        @Override
        public Optional<R> maybeR() {
            return Optional.of(this.value);
        }
    }
}
