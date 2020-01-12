// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import io.vlingo.common.Completes;

public class Returns<T> {

    private Object value;

    private Returns(Object value) {
        this.value = value;
    }

    public static <T> Returns<T> value(final Completes<T> completes) {
        return new Returns<>(completes);
    }

    public static <T> Returns<T> value(final Future<T> future) {
        return new Returns<>(future);
    }

    public static <T> Returns<T> value(final CompletableFuture<T> completableFuture) {
        return new Returns<>(completableFuture);
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

    @SuppressWarnings("unchecked")
    public Completes<T> asCompletes() {
        if(!isCompletes()) {
            throw new IllegalStateException(MessageFormat.format("The value type should be Completes<T> but was {0}.", value.getClass().getName()));
        }
        return (Completes<T>) value;
    };

    @SuppressWarnings("unchecked")
    public Future<T> asFuture() {
        if(!isFuture()) {
            throw new IllegalStateException(MessageFormat.format("The value type should be Future<T> but was {0}.", value.getClass().getName()));
        }
        return (Future<T>) value;
    }

    @SuppressWarnings("unchecked")
    public CompletableFuture<T> asCompletableFuture() {
        if(!isCompletableFuture()) {
            throw new IllegalStateException(MessageFormat.format("The value type should be CompletableFuture<T> but was {0}.", value.getClass().getName()));
        }
        return (CompletableFuture<T>) value;
    }
}
