// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Completes<T> {
  static <T> Completes<T> using(final Scheduler scheduler) {
    return new BasicCompletes<T>(scheduler);
  }

  static <T> Completes<T> withSuccess(final T outcome) {
    return new BasicCompletes<T>(outcome);
  }

  static <T> Completes<T> withFailure(final T outcome) {
    return new BasicCompletes<T>(outcome);
  }

  static <T> Completes<T> withFailure() {
    return new BasicCompletes<T>((T) null);
  }

  Completes<T> after(final Supplier<T> supplier);
  Completes<T> after(final Supplier<T> supplier, final long timeout);
  Completes<T> after(final Supplier<T> supplier, final long timeout, final T timedOutValue);
  Completes<T> after(final Consumer<T> consumer);
  Completes<T> after(final Consumer<T> consumer, final long timeout);
  Completes<T> after(final Consumer<T> consumer, final long timeout, final T timedOutValue);
  Completes<T> andThen(final Consumer<T> consumer);
  Completes<T> atLast(final Consumer<T> consumer);
  Completes<T> atLast(final Supplier<T> supplier);
  T await();
  T await(final long timeout);
  boolean hasOutcome();
  T outcome();
  <O> Completes<O> with(final O outcome);
}
