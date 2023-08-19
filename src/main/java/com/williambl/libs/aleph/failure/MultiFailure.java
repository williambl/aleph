package com.williambl.libs.aleph.failure;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public record MultiFailure(String description, @Unmodifiable List<Failure> causes) implements Failure {
    public MultiFailure(String description, List<Failure> causes) {
        this.description = description;
        this.causes = List.copyOf(causes);
    }

    public MultiFailure(List<Failure> failures) {
        this(
                "Multiple failures:\n" + failures.stream().map(Failure::description).map(d -> " "+d).collect(Collectors.joining("\n")),
                failures);
    }


    @Override
    public Optional<Failure> cause() {
        return Optional.empty(); // perhaps this should return the first element?
    }

    @Override
    public Optional<Throwable> throwable() {
        return Optional.empty();
    }

    public static class FailureCollector<T extends Failure> implements Collector<T, List<Failure>, MultiFailure> {
        private static final Set<Characteristics> CHARACTERISTICS = Set.of();
        private final @Nullable Function<List<Failure>, String> descriptionFunc;

        public FailureCollector(@Nullable Function<List<Failure>, String> descriptionFunc) {
            this.descriptionFunc = descriptionFunc;
        }

        public FailureCollector() {
            this(null);
        }

        @Override
        public Supplier<List<Failure>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<Failure>, T> accumulator() {
            return List::add;
        }

        @Override
        public BinaryOperator<List<Failure>> combiner() {
            return (a, b) -> {
                a.addAll(b);
                return a;
            };
        }

        @Override
        public Function<List<Failure>, MultiFailure> finisher() {
            return this.descriptionFunc == null ? MultiFailure::new : failures -> new MultiFailure(this.descriptionFunc.apply(failures), failures);
        }

        @Override
        public Set<Characteristics> characteristics() {
            return CHARACTERISTICS;
        }
    }
}
