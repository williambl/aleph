package com.williambl.libs.aleph.either;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

/**
 * A union - holds a value of either of two types, designated 'left' and 'right'. A little like {@link Optional},
 * but with two types (one of which will always be present).
 *
 * @param <L> the type of the left value
 * @param <R> the type of the right value
 */
public abstract sealed class Either<L, R> {
    /**
     * Creates a left Either with the given value.
     * @param value the value
     * @return      the Either
     */
    public static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    /**
     * Creates a right Either with the given value.
     * @param value the value
     * @return      the Either
     */
    public static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    /**
     * Creates an Either from the left value if it exists, or else the right value.
     * @param left  the left value, or null
     * @param right the right value
     * @return      the Either
     */
    public static <L, R> Either<L, R> of(@Nullable L left, R right) {
        return left == null ? right(right) : left(left);
    }

    /**
     * Creates an Either from the contents of the left optional if present, or else get the right value.
     * @param leftOpt  the left value optional
     * @param rightSup the right value supplier
     * @return         the Either
     */
    public static <L, R> Either<L, R> of(Optional<L> leftOpt, Supplier<R> rightSup) {
        return leftOpt.<Either<L, R>>map(Either::left).orElseGet(() -> Either.right(rightSup.get()));
    }

    /**
     * Maps both sides of the Either.
     * @param leftFunc  the function to map the left side
     * @param rightFunc the function to map the right side
     * @return          the mapped Either
     */
    public abstract <L1, R1> Either<L1, R1> mapBoth(Function<L, L1> leftFunc, Function<R, R1> rightFunc);

    /**
     * Maps the left side of the Either.
     * @param func  the function to map the left side
     * @return      the mapped Either
     */
    public <L1> Either<L1, R> mapLeft(Function<L, L1> func) {
        return this.mapBoth(func, Function.identity());
    }

    /**
     * Maps the right side of the Either.
     * @param func  the function to map the right side
     * @return      the mapped Either
     */
    public <R1> Either<L, R1> mapRight(Function<R, R1> func) {
        return this.mapBoth(Function.identity(), func);
    }

    /**
     * Flatmaps both sides of the Either.
     * @param leftFunc  the function to flatmap the left side
     * @param rightFunc the function to flatmap the right side
     * @return          the flatmapped Either
     */
    public abstract <L1, R1> Either<L1, R1> flatMapBoth(Function<L, Either<L1, R1>> leftFunc, Function<R, Either<L1, R1>> rightFunc);

    /**
     * Flatmaps the left side of the Either.
     * @param func  the function to flatmap the left side
     * @return      the flatmapped Either
     */
    public <L1> Either<L1, R> flatMapLeft(Function<L, Either<L1, R>> func) {
        return this.flatMapBoth(func, Either::right);
    }

    /**
     * Flatmaps the right side of the Either.
     * @param func  the function to flatmap the right side
     * @return      the flatmapped Either
     */
    public <R1> Either<L, R1> flatMapRight(Function<R, Either<L, R1>> func) {
        return this.flatMapBoth(Either::left, func);
    }

    /**
     * Maps the Either to a value of a single type.
     * @param leftFunc  the function to map the left side
     * @param rightFunc the function to map the right side
     * @return          the mapped value
     */
    public abstract <T> T map(Function<L, T> leftFunc, Function<R, T> rightFunc);

    /**
     * Consumes either side of the Either.
     * @param leftFunc  the function to consume the left side
     * @param rightFunc the function to consume the right side
     */
    public abstract void consume(Consumer<L> leftFunc, Consumer<R> rightFunc);

    /**
     * Whether this Either holds a left value.
     * @return whether this Either holds a left value
     */
    public abstract boolean isLeft();

    /**
     * Whether this Either holds a right value.
     * @return whether this Either holds a right value
     */
    public abstract boolean isRight();

    /**
     * Gets the left value of this Either.
     * @return the left value
     * @throws NoSuchElementException if this Either does not hold a left value
     */
    public abstract L left();

    /**
     * Gets the right value of this Either.
     * @return the right value
     * @throws NoSuchElementException if this Either does not hold a right value
     */
    public abstract R right();

    /**
     * Gets the left value of this Either wrapped in an Optional, or an empty Optional if there is no left value.
     * @return the left value wrapped in an Optional
     */
    public abstract Optional<L> maybeL();

    /**
     * Gets the right value of this Either wrapped in an Optional, or an empty Optional if there is no right value.
     * @return the right value wrapped in an Optional
     */
    public abstract Optional<R> maybeR();

    private static final class Left<L, R> extends Either<L, R> {
        private final L value;

        private Left(L value) {
            this.value = value;
        }

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

    private static final class Right<L, R> extends Either<L, R> {
        private final R value;

        private Right(R value) {
            this.value = value;
        }

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
