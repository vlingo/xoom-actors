// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BasicCompletes<T> implements Completes<T>, Scheduled {
  private Cancellable cancellable;
  private final AtomicBoolean completed;
  private Consumer<T> consumer;
  private final AtomicReference<T> outcome;
  private final Scheduler scheduler;
  private Supplier<T> supplier;
  private T timedOutValue;
  private long timeout;

  BasicCompletes(final Scheduler scheduler) {
    this.scheduler = scheduler;
    this.consumer = null;
    this.completed = new AtomicBoolean(false);
    this.outcome = new AtomicReference<>();
    this.supplier = null;
  }

  BasicCompletes(final T outcome) {
    this.scheduler = null;
    this.consumer = null;
    this.completed = new AtomicBoolean(true);
    this.outcome = new AtomicReference<>(outcome);
    this.supplier = null;
  }

  @Override
  public Completes<T> after(final Supplier<T> supplier) {
    after(supplier, -1L, null);
    return this;
  }

  @Override
  public Completes<T> after(final Supplier<T> supplier, final long timeout) {
    after(supplier, timeout, null);
    return this;
  }

  @Override
  public Completes<T> after(final Supplier<T> supplier, final long timeout, final T timedOutValue) {
    this.supplier = supplier;
    this.timeout = timeout;
    this.timedOutValue = timedOutValue;
    startTimer();
    return this;
  }

  @Override
  public Completes<T> andThen(final Consumer<T> consumer) {
    if (this.consumer != null) {
      throw new IllegalStateException("Consumer already set making andThen() invalid.");
    }
    this.consumer = consumer;
    return this;
  }

  @Override
  public Completes<T> after(final Consumer<T> consumer) {
    after(consumer, -1L, null);
    return this;
  }

  @Override
  public Completes<T> after(final Consumer<T> consumer, final long timeout) {
    after(consumer, timeout, null);
    return this;
  }

  @Override
  public Completes<T> after(final Consumer<T> consumer, final long timeout, final T timedOutValue) {
    this.consumer = consumer;
    this.timeout = timeout;
    startTimer();
    return this;
  }

  @Override
  public T outcome() {
    return outcome.get();
  }

  @Override
  public void with(final T outcome) {
    this.outcome.set(outcome);
    
    if (cancellable != null) {
      cancellable.cancel();
    }

    complete(false);
  }

  @Override
  public void intervalSignal(final Scheduled scheduled, final Object data) {
    complete(true);
  }

  private void complete(final boolean timedOut) {
    if (completed.compareAndSet(false, true)) {
      if (timedOut) {
        outcome.set(timedOutValue);
      }

      if (supplier != null) {
        outcome.set(supplier.get());
      }

      if (consumer != null) {
        consumer.accept(outcome.get());
      }
    }
  }

  private void startTimer() {
    if (timeout > 0) {
      // 2L delayBefore prevents timeout until after return from here
      cancellable = scheduler.scheduleOnce(this, null, 2L, timeout);
    }
  }
}
