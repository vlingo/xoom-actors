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

  private Completes completes;

  @Override
  public <O> Completes<O> andThen(final long timeout, final O failedOutcomeValue, final Function<Object, O> function) {
    completes().andThen(timeout, failedOutcomeValue, function);
    setOutcome();
    return completes;
  }

  @Override
  public <O> Completes<O> andThen(final O failedOutcomeValue, final Function<Object, O> function) {
    completes().andThen(failedOutcomeValue, function);
    setOutcome();
    return completes;
  }

  @Override
  public <O> Completes<O> andThen(final long timeout, final Function<Object, O> function) {
    completes().andThen(timeout, function);
    setOutcome();
    return completes;
  }

  @Override
  public <O> Completes<O> andThen(final Function<Object, O> function) {
    completes().andThen(function);
    setOutcome();
    return completes;
  }

  @Override
  public <F,O> O andThenTo(final long timeout, final F failedOutcomeValue, final Function<Object,O> function) {
    completes = (Completes) completes().andThenTo(timeout, failedOutcomeValue, function);
    setOutcome();
    return (O) completes;
  }

  @Override
  public <F,O> O andThenTo(final F failedOutcomeValue, final Function<Object,O> function) {
    completes = (Completes) completes().andThenTo(failedOutcomeValue, function);
    setOutcome();
    return (O) completes;
  }

  @Override
  public <O> O andThenTo(final long timeout, final Function<Object,O> function) {
    completes = (Completes) completes().andThenTo(timeout, function);
    setOutcome();
    return (O) completes;
  }

  @Override
  public <O> O andThenTo(final Function<Object,O> function) {
    completes = (Completes) completes().andThenTo(function);
    setOutcome();
    return (O) completes;
  }

  @Override
  public Completes<Object> andThenConsume(final Consumer<Object> consumer) {
    completes = completes().andThenConsume(consumer);
    setOutcome();
    return completes;
  }

  @Override
  public Completes<Object> andThenConsume(final long timeout, final Consumer<Object> consumer) {
    completes = completes().andThenConsume(timeout, consumer);
    setOutcome();
    return completes;
  }

  @Override
  public Completes<Object> andThenConsume(final Object failedOutcomeValue, final Consumer<Object> consumer) {
    completes = completes().andThenConsume(failedOutcomeValue, consumer);
    setOutcome();
    return completes;
  }

  @Override
  public Completes<Object> andThenConsume(final long timeout, final Object failedOutcomeValue, final Consumer<Object> consumer) {
    completes = completes().andThenConsume(timeout, failedOutcomeValue, consumer);
    setOutcome();
    return completes;
  }

  @Override
  public Completes<Object> otherwise(final Function<Object,Object> function) {
    completes = completes().otherwise(function);
    setOutcome();
    return completes;
  }

  @Override
  public Completes<Object> otherwiseConsume(final Consumer<Object> function) {
    completes = completes().otherwiseConsume(function);
    setOutcome();
    return completes;
  }

  @Override
  public Completes<Object> recoverFrom(final Function<Exception,Object> function) {
    completes = completes().recoverFrom(function);
    setOutcome();
    return completes;
  }

  @Override
  public <O> Completes<O> andFinally() {
    completes = completes().andFinally();
    setOutcome();
    return completes;
  }

  @Override
  public <O> Completes<O> andFinally(final Function<Object, O> function) {
    completes = completes().andFinally(function);
    setOutcome();
    return completes;
  }

  @Override
  public void andFinallyConsume(final Consumer<Object> consumer) {
    completes().andFinallyConsume(consumer);
    setOutcome();
  }

  @Override
  public Object await() {
    return completes().await();
  }

  @Override
  public Object await(final long timeout) {
    return completes().await(timeout);
  }

  @Override
  public boolean isCompleted() {
    return completes().isCompleted();
  }

  @Override
  public boolean hasFailed() {
    return completes().hasFailed();
  }

  @Override
  public void failed() {
    completes().failed();
  }

  @Override
  public boolean hasOutcome() {
    return completes().hasOutcome();
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

  private Completes completes() {
    if (completes == null) {
      completes = Completes.using(null);
    }
    return completes;
  }

  private void setOutcome() {
    if (__internal__outcomeSet && !completes.hasOutcome()) {
      completes.with(outcome());
    }
  }
}
