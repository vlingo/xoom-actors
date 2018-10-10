// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class BasicCompletes<T> implements Completes<T> {
  protected final ActiveState<T> state;

  public BasicCompletes(final Scheduler scheduler) {
    this(new BasicActiveState<T>(scheduler));
  }

  public BasicCompletes(final T outcome, final boolean succeeded) {
    this(new BasicActiveState<T>(), outcome, succeeded);
  }

  public BasicCompletes(final T outcome) {
    this(new BasicActiveState<T>(), outcome);
  }

  protected BasicCompletes(final ActiveState<T> state) {
    this.state = state;
  }

  protected BasicCompletes(final ActiveState<T> state, final T outcome, final boolean succeeded) {
    this.state = state;
    if (succeeded) {
      this.state.completedWith(outcome);
    } else {
      this.state.failedValue(outcome);
      this.state.failed();
    }
  }

  protected BasicCompletes(final ActiveState<T> state, final T outcome) {
    this.state = state;
    this.state.outcome(outcome);
  }

  @Override
  public Completes<T> after(final Function<T,T> function) {
    return after(-1L, null, function);
  }

  @Override
  public Completes<T> after(final long timeout, final Function<T,T> function) {
    return after(timeout, null, function);
  }

  @Override
  public Completes<T> after(final T failedOutcomeValue, final Function<T,T> function) {
    return after(-1L, failedOutcomeValue, function);
  }

  @Override
  public Completes<T> after(final long timeout, final T failedOutcomeValue, final Function<T,T> function) {
    state.failedValue(failedOutcomeValue);
    state.action(Action.with(function));
    if (state.isCompleted()) {
      state.completeActions();
    } else {
      state.startTimer(timeout);
    }
    return this;
  }

  @Override
  public Completes<T> andThen(final Function<T,T> function) {
    state.action(Action.with(function));
    if (state.isCompleted()) {
      state.completeActions();
    }
    return this;
  }

  @Override
  public Completes<T> atLast(final Function<T,T> function) {
    state.action(Action.with(function));
    if (state.isCompleted()) {
      state.completeActions();
    }
    return this;
  }

  @Override
  public Completes<T> otherwise(final Function<T,T> function) {
    state.failureAction(function);
    return this;
  }

  @Override
  public Completes<T> exception(final Function<Exception,T> function) {
    state.exceptionAction(function);
    return this;
  }

  @Override
  public Completes<T> consumeAfter(final Consumer<T> consumer) {
    return consumeAfter(-1, null, consumer);
  }

  @Override
  public Completes<T> consumeAfter(final long timeout, final Consumer<T> consumer) {
    return consumeAfter(timeout, null, consumer);
  }

  @Override
  public Completes<T> consumeAfter(final T failedOutcomeValue, final Consumer<T> consumer) {
    return consumeAfter(-1, failedOutcomeValue, consumer);
  }

  @Override
  public Completes<T> consumeAfter(final long timeout, final T failedOutcomeValue, final Consumer<T> consumer) {
    state.failedValue(failedOutcomeValue);
    state.action(Action.with(consumer));
    if (state.isCompleted()) {
      state.completeActions();
    } else {
      state.startTimer(timeout);
    }
    return this;
  }

  @Override
  public Completes<T> andThenConsume(final Consumer<T> consumer) {
    state.action(Action.with(consumer));
    if (state.isCompleted()) {
      state.completeActions();
    }
    return this;
  }

  @Override
  public Completes<T> atLastConsume(final Consumer<T> consumer) {
    state.action(Action.with(consumer));
    if (state.isCompleted()) {
      state.completeActions();
    }
    return this;
  }

  @Override
  public T await() {
    return await(-1);
  }

  @Override
  public T await(final long timeout) {
    long countDown = timeout;
    while (true) {
      if (isCompleted()) {
        return outcome();
      }
      try {
        Thread.sleep((countDown >= 0 && countDown < 100) ? countDown : 100);
      } catch (Exception e) {
        // ignore
      }
      if (isCompleted()) {
        return outcome();
      }
      if (timeout >= 0) {
        countDown -= 100;
        if (countDown <= 0) {
          return null;
        }
      }
    }
  }

  @Override
  public boolean isCompleted() {
    return state.isCompleted();
  }

  @Override
  public boolean hasFailed() {
    return state.hasFailed();
  }

  @Override
  public void failed() {
    with(state.failedValue());
  }

  @Override
  public boolean hasOutcome() {
    return state.hasOutcome();
  }

  @Override
  public T outcome() {
    return state.outcome();
  }

  @Override
  public Completes<T> repeat() {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> with(final O outcome) {
    if (!state.handleFailure((T) outcome)) {
      state.completedWith((T) outcome);
    }

    return (Completes<O>) this;
  }

  protected static class Action<T> {
    private final T defaultValue;
    private final boolean hasDefaultValue;
    private final Object function;

    static <T> Action<T> with(final Object function) {
      return new Action<T>(function);
    }

    static <T> Action<T> with(final Object function, final T defaultValue) {
      return new Action<T>(function, defaultValue);
    }

    Action(final Object function, final T defaultValue) {
      this.function = function;
      this.defaultValue = defaultValue;
      this.hasDefaultValue = true;
    }

    Action(final Object function) {
      this.function = function;
      this.defaultValue = null;
      this.hasDefaultValue = false;
    }

    @SuppressWarnings("unchecked")
    <F> F function() {
      return (F) function;
    }

    @SuppressWarnings("unchecked")
    Consumer<T> asConsumer() {
      return (Consumer<T>) function;
    }

    boolean isConsumer() {
      return (function instanceof Consumer);
    }

    @SuppressWarnings("unchecked")
    Function<T,T> asFunction() {
      return (Function<T,T>) function;
    }

    boolean isFunction() {
      return (function instanceof Function);
    }
  }

  protected static interface ActiveState<T> {
    boolean hasAction();
    void action(final Action<T> action);
    Action<T> action();
    void cancelTimer();
    boolean isCompleted();
    void completeActions();
    void completedWith(final T outcome);
    boolean hasFailed();
    void failed();
    void failedValue(final T failedOutcomeValue);
    T failedValue();
    void failureAction(final Function<T,T> function);
    void failureAction();
    boolean handleFailure(final T outcome);
    void exceptionAction(final Function<Exception,T> function);
    void handleException(final Exception e);
    boolean hasException();
    boolean hasOutcome();
    boolean outcomeMustDefault();
    void outcome(final T outcome);
    T outcome();
    boolean isRepeatable();
    void repeat();
    void startTimer(final long timeout);
  }

  protected static class BasicActiveState<T> implements ActiveState<T>, Scheduled {
    private static final Object UnfailedValue = new Object();

    private Queue<Action<T>> actions;
    private Cancellable cancellable;
    private final AtomicBoolean completed;
    private final AtomicBoolean completing;
    private final AtomicBoolean executingActions;
    private final AtomicBoolean failed;
    private T failedOutcomeValue;
    private Function<T,Completes<?>> failureAction;
    private AtomicReference<Exception> exception;
    private Function<Exception,?> exceptionAction;
    private final AtomicReference<T> outcome;
    private Scheduler scheduler;
    private final AtomicBoolean timedOut;

    @SuppressWarnings("unchecked")
    protected BasicActiveState(final Scheduler scheduler) {
      this.scheduler = scheduler;
      this.actions = new ConcurrentLinkedQueue<>();
      this.completed = new AtomicBoolean(false);
      this.completing = new AtomicBoolean(false);
      this.executingActions = new AtomicBoolean(false);
      this.failed = new AtomicBoolean(false);
      this.failedOutcomeValue = (T) UnfailedValue;
      this.exception = new AtomicReference<>(null);
      this.outcome = new AtomicReference<>(null);
      this.timedOut = new AtomicBoolean(false);
    }

    protected BasicActiveState() {
      this(null);
    }

    @Override
    public boolean hasAction() {
      return actions.peek() != null;
    }

    @Override
    public void action(final Action<T> action) {
      actions.add(action);
    }

    @Override
    public Action<T> action() {
      return actions.poll();
    }

    public void cancelTimer() {
      if (cancellable != null) {
        cancellable.cancel();
        cancellable = null;
      }
    }

    @Override
    public boolean isCompleted() {
      return completed.get();
    }

    @Override
    public void completeActions() {
      if (completing.compareAndSet(false, true)) {
        executeActions();

        completed.set(true);

        completing.set(false);
      }
    }

    @Override
    public void completedWith(final T outcome) {
      if (completing.compareAndSet(false, true)) {
        cancelTimer();

        if (!timedOut.get()) {
          this.outcome.set(outcome);
        }

        executeActions();

        completed.set(true);

        completing.set(false);
      }
    }

    @Override
    public boolean hasFailed() {
      return failed.get();
    }

    @Override
    public void failed() {
      handleFailure(failedOutcomeValue);
    }

    @Override
    public void failedValue(final T failedOutcomeValue) {
      this.failedOutcomeValue = failedOutcomeValue;
    }

    @Override
    public T failedValue() {
      return failedOutcomeValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void failureAction(final Function<T,T> function) {
      this.failureAction = Function.class.cast(function); // this really, really stinks
      if (isCompleted() && hasFailed()) {
        failureAction();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void failureAction() {
      failed.set(true);
      if (failureAction != null) {
        outcome.set((T) failureAction.apply(outcome.get()));
      }
    }

    @Override
    public boolean handleFailure(final T outcome) {
      if (isCompleted() && hasFailed()) {
        return true; // already reached below
      }
      boolean handle = false;
      if (outcome == failedOutcomeValue) {
        handle = true;
      } else if (outcome != null && failedOutcomeValue != null && failedOutcomeValue.equals(outcome)) {
        handle = true;
      }
      if (handle) {
        failed.set(true);
        actions.clear();
        this.outcome.set(failedOutcomeValue);
        completed.set(true);
        failureAction();
      }
      return handle;
    }

    @Override
    public void exceptionAction(final Function<Exception,T> function) {
      exceptionAction = function;
      if (hasException()) {
        handleException(exception.get());
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleException(final Exception e) {
      exception.set(e);
      if (exceptionAction != null) {
        failed.set(true);
        actions.clear();
        outcome.set((T) exceptionAction.apply(e));
        completed.set(true);
      }
    }

    @Override
    public boolean hasException() {
      return exception.get() != null;
    }

    @Override
    public boolean hasOutcome() {
      return outcome.get() != null;
    }

    @Override
    public boolean outcomeMustDefault() {
      return outcome() == null;
    }

    @Override
    public void outcome(final T outcome) {
      this.outcome.set(outcome);
    }

    @Override
    public T outcome() {
      return outcome.get();
    }

    @Override
    public boolean isRepeatable() {
      return false;
    }

    @Override
    public void repeat() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void startTimer(final long timeout) {
      if (timeout > 0 && scheduler != null) {
        // 2L delayBefore prevents timeout until after return from here
        cancellable = scheduler.scheduleOnce(this, null, 2L, timeout);
      }
    }

    @Override
    public void intervalSignal(final Scheduled scheduled, final Object data) {
      timedOut.set(true);
      failed();
    }

    protected void executeActions() {
      if (executingActions.compareAndSet(false, true))
        ;
      while (hasAction()) {
        final Action<T> action = action();
        if (action.hasDefaultValue && outcomeMustDefault()) {
          outcome(action.defaultValue);
        } else {
          try {
            if (action.isConsumer()) {
              action.asConsumer().accept(outcome.get());
            } else if (action.isFunction()) {
              outcome.set(action.asFunction().apply(outcome.get()));
            }
          } catch (Exception e) {
            handleException(e);
            break;
          }
        }
      }
      executingActions.set(false);
    }
  }
}
