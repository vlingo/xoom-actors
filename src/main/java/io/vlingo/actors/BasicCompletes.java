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
  private final AtomicReference<Outcome> outcome;
  private final State state;

  public BasicCompletes(final Scheduler scheduler) {
    this.outcome = new AtomicReference<>(null);
    this.state = new State(scheduler);
  }

  public BasicCompletes(final T outcome) {
    this.outcome = new AtomicReference<>(new Outcome(outcome));
    this.state = new State();
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
    state.supplier = supplier;
    state.timedOutValue = timedOutValue;
    if (state.isCompleted() && outcome.get() != null) {
      outcome.set(new Outcome(state.supplier.get()));
    } else {
      startTimer(timeout);
    }
    return this;
  }

  @Override
  public Completes<T> andThen(final Consumer<T> consumer) {
    state.andThen = consumer;
    if (state.isCompleted() && outcome.get() != null) {
      state.andThen.accept(outcome.get().data);
    }
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
    state.consumer = consumer;
    state.timedOutValue = timedOutValue;
    if (state.isCompleted() && outcome.get() != null) {
      state.consumer.accept(outcome.get().data);
    } else {
      startTimer(timeout);
    }
    return this;
  }

  @Override
  public boolean hasOutcome() {
    return outcome.get() != null;
  }

  @Override
  public T outcome() {
    return outcome.get().data;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> with(final O outcome) {
    if (state == null) {
      this.outcome.set(new Outcome((T) outcome));
    } else {
      completedWith(false, (T) outcome);
    }

    return (Completes<O>) this;
  }

  @Override
  public void intervalSignal(final Scheduled scheduled, final Object data) {
    completedWith(true, null);
  }

  BasicCompletes() {
    this.outcome = new AtomicReference<>(null);
    this.state = null;
  }

  void clearOutcome() {
    outcome.set(null);
  }

  private void completedWith(final boolean timedOut, final T outcome) {
    if (state.completed.compareAndSet(false, true)) {
      this.outcome.set(new Outcome(outcome));

      state.cancelTimer();

      if (timedOut) {
        this.outcome.set(new Outcome(state.timedOutValue));
      }

      if (state.supplier != null) {
        this.outcome.set(new Outcome(state.supplier.get()));
      }

      if (state.consumer != null) {
        state.consumer.accept(this.outcome.get().data);
      }

      if (state.andThen != null) {
        state.andThen.accept(this.outcome.get().data);
      }
    }
  }

  private void startTimer(final long timeout) {
    if (timeout > 0) {
      // 2L delayBefore prevents timeout until after return from here
      state.cancellable = state.scheduler.scheduleOnce(this, null, 2L, timeout);
    }
  }

  private class Outcome {
    private T data;

    private Outcome(final T data) {
      this.data = data;
    }
  }

  private class State {
    private Consumer<T> andThen;
    private Cancellable cancellable;
    private final AtomicBoolean completed;
    private Consumer<T> consumer;
    private Scheduler scheduler;
    private Supplier<T> supplier;
    private T timedOutValue;

    private State(final Scheduler scheduler) {
      this.scheduler = scheduler;
      this.andThen = null;
      this.consumer = null;
      this.completed = new AtomicBoolean(false);
      this.supplier = null;
    }

    private State() {
      this(null);
    }

    private void cancelTimer() {
      if (cancellable != null) {
        cancellable.cancel();
        cancellable = null;
      }
    }

    private boolean isCompleted() {
      return completed.get();
    }
  }
}
