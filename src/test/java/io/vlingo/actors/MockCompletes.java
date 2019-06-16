// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.common.BasicCompletes;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduler;

public class MockCompletes<T> extends BasicCompletes<T> {
  private final AtomicReference<T> outcome = new AtomicReference<>(null);
  private final AtomicInteger withCount = new AtomicInteger(0);
  private final AccessSafely safely;

  public MockCompletes(final int times) {
    super((Scheduler) null);

    this.safely = AccessSafely
            .afterCompleting(times)
            .writingWith("outcome", (T newValue) -> {
              this.outcome.set(newValue);
              this.withCount.incrementAndGet();
            })
            .readingWith("outcome", this.outcome::get)
            .readingWith("count", withCount::get);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> with(final O outcome) {
    this.safely.writeUsing("outcome", outcome);
    return (Completes<O>) this;
  }

  @Override
  public T outcome() {
    return this.safely.readFrom("outcome");
  }

  public int getWithCount() {
    return this.safely.readFrom("count");
  }
}
