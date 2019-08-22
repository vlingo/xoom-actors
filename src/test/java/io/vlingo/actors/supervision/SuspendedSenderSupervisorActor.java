// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Supervised;
import io.vlingo.actors.SupervisionStrategy;
import io.vlingo.actors.Supervisor;
import io.vlingo.actors.testkit.AccessSafely;

public class SuspendedSenderSupervisorActor extends Actor implements Supervisor, FailureControlSender {
  public static SuspendedSenderSupervisorActor instance;

  private SuspendedSenderSupervisorResults results;

  private FailureControl failureControl;
  private int times;

  public SuspendedSenderSupervisorActor(final SuspendedSenderSupervisorResults results) {
    instance = this;
    this.results = results;
  }

  private final SupervisionStrategy strategy =
          new SupervisionStrategy() {
            @Override
            public int intensity() {
              return SupervisionStrategy.ForeverIntensity;
            }

            @Override
            public long period() {
              return SupervisionStrategy.ForeverPeriod;
            }

            @Override
            public Scope scope() {
              return Scope.One;
            }
          };

  @Override
  public void inform(final Throwable throwable, final Supervised supervised) {
    for (int idx = 1; idx <= times; ++idx) {
      failureControl.afterFailureCount(idx);
    }

    supervised.resume();

    results.access.writeUsing("informedCount", 1);
  }

  @Override
  public SupervisionStrategy supervisionStrategy() {
    return strategy;
  }

  @Override
  public void sendUsing(final FailureControl failureControl, final int times) {
    this.failureControl = failureControl;
    this.times = times;
  }

  public static class SuspendedSenderSupervisorResults {
    public AccessSafely access = afterCompleting(0);

    public AtomicInteger informedCount = new AtomicInteger(0);

    public AccessSafely afterCompleting(final int times) {
      access =
        AccessSafely.afterCompleting(times)
        .writingWith("informedCount", (Integer increment) -> informedCount.incrementAndGet())
        .readingWith("informedCount", () -> informedCount.get());

      return access;
    }
  }
}
