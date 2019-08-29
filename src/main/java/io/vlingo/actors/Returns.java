package io.vlingo.actors;

import io.vlingo.common.Completes;

import java.text.MessageFormat;
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
        if(!isCompletes()) {
            throw new IllegalStateException(MessageFormat.format("The value type should be Completes<T> but was {0}.", value.getClass().getName()));
        }
        return (Completes<T>) value;
    };

    public Future<T> asFuture() {
        if(!isFuture()) {
            throw new IllegalStateException(MessageFormat.format("The value type should be Future<T> but was {0}.", value.getClass().getName()));
        }
        return (Future<T>) value;
    }

    public CompletableFuture<T> asCompletableFuture() {
        if(!isCompletableFuture()) {
            throw new IllegalStateException(MessageFormat.format("The value type should be CompletableFuture<T> but was {0}.", value.getClass().getName()));
        }
        return (CompletableFuture<T>) value;
    }
}
