// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;
import java.util.function.Function;

import io.vlingo.common.Completes;

class ResultCompletes implements Completes<Object> {
  public Completes<Object> __internal__clientCompletes;
  public Object __internal__outcome = null;
  public boolean __internal__outcomeSet = false;


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
  public <O> Completes<O> andThen(long timeout, O failedOutcomeValue, Function<Object, O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <O> Completes<O> andThen(O failedOutcomeValue, Function<Object, O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <O> Completes<O> andThen(long timeout, Function<Object, O> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <O> Completes<O> andThen(Function<Object, O> function) {
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
    throw new UnsupportedOperationException();
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

  public Completes<Object> clientCompletes() {
    return __internal__clientCompletes;
  }

  public void reset(final Completes<Object> clientCompletes) {
    this.__internal__clientCompletes = clientCompletes;
    this.__internal__outcome = null;
    this.__internal__outcomeSet = false;
  }
}
