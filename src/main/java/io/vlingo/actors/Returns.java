package io.vlingo.actors;

import io.vlingo.common.Completes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Returns<T> {

    private Object value;

    private Returns(Object value) {
        this.value = value;
    }

    public static <T> Returns value(final Completes<T> completes) {
        return new Returns(completes);
    }

    public static <T> Returns value(final Future<T> future) {
        return new Returns(future);
    }

    public static <T> Returns value(final CompletableFuture<T> completableFuture) {
        return new Returns(completableFuture);
    }

    public boolean isCompletes() {
        return value instanceof Completes;
    }

    public boolean isFuture() {
        return value instanceof Future;
    }

    public boolean isCompletableFuture() {
        return value instanceof CompletableFuture;
    }

    public Completes<T> asCompletes() {
        return (Completes<T>) value;
    };

    public Future<T> asFuture() {
        return (Future<T>) value;
    }

    public CompletableFuture<T> asCompletableFuture() {
        return (CompletableFuture<T>) value;
    }
}
