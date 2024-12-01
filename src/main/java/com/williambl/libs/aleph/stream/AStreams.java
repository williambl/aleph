package com.williambl.libs.aleph.stream;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class AStreams {
    public static <A, B> void zipForEach(Stream<A> a, Stream<B> b, BiConsumer<A, B> forEach) {
        var aList = a.toList();
        var bList = b.toList();
        for (var i = 0; i < aList.size() && i < bList.size(); i++) {
            forEach.accept(aList.get(i), bList.get(i));
        }
    }

    public static <A, B, C> C zipReduce(Stream<A> a, Stream<B> b, C initial, ZipReduceFunction<A, B, C> acc) {
        var aList = a.toList();
        var bList = b.toList();
        C value = initial;
        for (var i = 0; i < aList.size() && i < bList.size(); i++) {
            value = acc.reduce(aList.get(i), bList.get(i), value);
        }

        return value;
    }

    @FunctionalInterface
    public interface ZipReduceFunction<A, B, C> {
        C reduce(A a, B b, C acc);
    }
}
