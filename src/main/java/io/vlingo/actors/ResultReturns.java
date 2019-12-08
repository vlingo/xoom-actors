// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import io.vlingo.common.Completes;

@SuppressWarnings({"rawtypes", "unchecked"})
class ResultReturns implements Completes<Object> {
  public Returns<Object> __internal__clientReturns;
  public Object __internal__outcome = null;
  public boolean __internal__outcomeSet = false;

  @Override
  public <O> Completes<O> andThen(final long timeout, final O failedOutcomeValue, final Function<Object, O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <O> Completes<O> andThen(final O failedOutcomeValue, final Function<Object, O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <O> Completes<O> andThen(final long timeout, final Function<Object, O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <O> Completes<O> andThen(final Function<Object, O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <F,O> O andThenTo(final long timeout, final F failedOutcomeValue, final Function<Object,O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <F,O> O andThenTo(final F failedOutcomeValue, final Function<Object,O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <O> O andThenTo(final long timeout, final Function<Object,O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <O> O andThenTo(final Function<Object,O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Completes<Object> andThenConsume(final Consumer<Object> consumer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Completes<Object> andThenConsume(final long timeout, final Consumer<Object> consumer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Completes<Object> andThenConsume(final Object failedOutcomeValue, final Consumer<Object> consumer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Completes<Object> andThenConsume(final long timeout, final Object failedOutcomeValue, final Consumer<Object> consumer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Completes<Object> otherwise(final Function<Object,Object> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Completes<Object> otherwiseConsume(final Consumer<Object> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Completes<Object> recoverFrom(final Function<Exception,Object> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <O> Completes<O> andFinally() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <O> Completes<O> andFinally(final Function<Object, O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void andFinallyConsume(final Consumer<Object> consumer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object await() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object await(final long timeout) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCompleted() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasFailed() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void failed() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasOutcome() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object outcome() {
    try {
      if (__internal__clientReturns.isCompletes()) {
        return __internal__clientReturns.asCompletes().outcome();
      } else if (__internal__clientReturns.isCompletableFuture()) {
        return __internal__clientReturns.asCompletableFuture().getNow(null);
      } else if (__internal__clientReturns.isFuture()) {
        return __internal__clientReturns.asFuture().get();
      }
    } catch (Exception e) {
      // fall through
    }

    throw new IllegalStateException("Unknown result type.");
  }

  @Override
  public Completes<Object> repeat() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Completes<Object> with(final Object outcome) {
    this.__internal__outcomeSet = true;
    this.__internal__outcome = outcome;
    return this;
  }

  public Returns<Object> clientReturns() {
    return __internal__clientReturns;
  }

  public void reset(final Returns<Object> clientReturns) {
    this.__internal__clientReturns = clientReturns;
    this.__internal__outcome = null;
    this.__internal__outcomeSet = false;
  }

  public CompletableFuture<Object> asCompletableFuture() {
    __internal__clientReturns.asCompletableFuture().thenApply(o -> this.__internal__outcome = o);
    return __internal__clientReturns.asCompletableFuture();
  }
}
