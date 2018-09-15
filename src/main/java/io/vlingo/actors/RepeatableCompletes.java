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

public class RepeatableCompletes<T> extends BasicCompletes<T> {

  public RepeatableCompletes(final Scheduler scheduler) {
    super(new RepeatableActiveState<T>(scheduler));
  }

  public RepeatableCompletes(final T outcome, final boolean succeeded) {
    super(new RepeatableActiveState<T>(), outcome, succeeded);
  }

  public RepeatableCompletes(final T outcome) {
    super(new RepeatableActiveState<T>(), outcome);
  }

  @Override
  public Completes<T> repeat() {
    if (state.isCompleted()) {
      state.repeat();
    }
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> with(O outcome) {
    state.outcome((T) outcome);
    state.repeat();
    return (Completes<O>) this;
  }

  protected static class RepeatableActiveState<T> extends BasicActiveState<T> {
    private final Queue<Action<T>> actionsBackup;
    private final Queue<T> pendingOutcomes;
    private final AtomicBoolean repeating;

    protected RepeatableActiveState(final Scheduler scheduler) {
      super(scheduler);
      this.actionsBackup = new ConcurrentLinkedQueue<>();
      this.pendingOutcomes = new ConcurrentLinkedQueue<>();
      this.repeating = new AtomicBoolean(false);
    }

    protected RepeatableActiveState() {
      this(null);
    }

    @Override
    public Action<T> action() {
      final Action<T> action = super.action();
      backUp(action);
      return action;
    }

    @Override
    public void outcome(final T outcome) {
      cancelTimer();
      pendingOutcomes.add(outcome);
    }

    @Override
    public void repeat() {
      if (repeating.compareAndSet(false, true)) {
        while (pendingOutcomes.peek() != null) {
          final T pendingOutcome = pendingOutcomes.poll();
          completedWith(pendingOutcome);
          restore();
        }
        repeating.set(false);
      }
    }

    private void backUp(final Action<T> action) {
      if (action != null) {
        actionsBackup.add(action);
      }
    }

    private void restore() {
      while (actionsBackup.peek() != null) {
        action(actionsBackup.poll());
      }
    }
  }
}
