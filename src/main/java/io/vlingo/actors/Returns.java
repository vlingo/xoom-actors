package io.vlingo.actors;

import io.vlingo.common.Completes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Returns<T> {

    private Completes<T> completes;
    private Future<T> future;
    private CompletableFuture<T> completableFuture;

    private Returns(final Completes<T> completes) {
        this.completes = completes;
    }

    private Returns(final Future<T> future) {
        this.future = future;
    }

    private Returns(final CompletableFuture<T> completableFuture) {
        this.completableFuture = completableFuture;
    }

    public static <T> Returns value(final Completes completes) {
        return new Returns(completes);
    }

    public static <T> Returns value(final Future<T> future) {
        return new Returns(future);
    }

    public static <T> Returns value(final CompletableFuture<T> completableFuture) {
        return new Returns(completableFuture);
    }

    public Completes<T> asCompletes() {
        return completes;
    };

    public Future<T> asFuture() {
        return future;
    }

    public CompletableFuture<T> asCompletableFuture() {
        return completableFuture;
    }
}
