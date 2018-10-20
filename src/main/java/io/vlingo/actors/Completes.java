// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Completes<T> {
  static <T> Completes<T> using(final Scheduler scheduler) {
    return new BasicCompletes<T>(scheduler);
  }

  static <T> Completes<T> withSuccess(final T outcome) {
    return new BasicCompletes<T>(outcome, true);
  }

  static <T> Completes<T> withFailure(final T outcome) {
    return new BasicCompletes<T>(outcome, false);
  }

  static <T> Completes<T> withFailure() {
    return new BasicCompletes<T>((T) null, false);
  }

  static <T> Completes<T> repeatableUsing(final Scheduler scheduler) {
    return new RepeatableCompletes<T>(scheduler);
  }

  static <T> Completes<T> repeatableWithSuccess(final T outcome) {
    return new RepeatableCompletes<T>(outcome, true);
  }

  static <T> Completes<T> repeatableWithFailure(final T outcome) {
    return new RepeatableCompletes<T>(outcome, false);
  }

  static <T> Completes<T> repeatableWithFailure() {
    return new RepeatableCompletes<T>((T) null, false);
  }

  <O> Completes<O> andThen(final long timeout, final T failedOutcomeValue, final Function<T,O> function);
  <O> Completes<O> andThen(final T failedOutcomeValue, final Function<T,O> function);
  <O> Completes<O> andThen(final long timeout, final Function<T,O> function);
  <O> Completes<O> andThen(final Function<T,O> function);

  Completes<T> andThenConsume(final long timeout, final T failedOutcomeValue, final Consumer<T> consumer);
  Completes<T> andThenConsume(final T failedOutcomeValue, final Consumer<T> consumer);
  Completes<T> andThenConsume(final long timeout, final Consumer<T> consumer);
  Completes<T> andThenConsume(final Consumer<T> consumer);

  <F,O> O andThenInto(final long timeout, final F failedOutcomeValue, final Function<T,O> function);
  <F,O> O andThenInto(final F failedOutcomeValue, final Function<T,O> function);
  <O> O andThenInto(final long timeout, final Function<T,O> function);
  <O> O andThenInto(final Function<T,O> function);

  Completes<T> otherwise(final Function<T,T> function);
  Completes<T> otherwiseConsume(final Consumer<T> consumer);
  Completes<T> recoverFrom(final Function<Exception,T> function);

  T await();
  T await(final long timeout);
  boolean isCompleted();
  boolean hasFailed();
  void failed();
  boolean hasOutcome();
  T outcome();
  Completes<T> repeat();
  <O> Completes<O> with(final O outcome);
}
