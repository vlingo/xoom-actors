// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
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

public class StopAllSupervisorActor extends Actor implements Supervisor {
  public static StopAllSupervisorActor instance;

  private StopAllSupervisorResult result;

  public StopAllSupervisorActor(final StopAllSupervisorResult result) {
    this.result = result;
    instance = this;
  }

  private final SupervisionStrategy strategy =
          new SupervisionStrategy() {
            @Override
            public int intensity() {
              return 5;
            }

            @Override
            public long period() {
              return 1000;
            }

            @Override
            public Scope scope() {
              return Scope.All;
            }
          };

  @Override
  public void inform(final Throwable throwable, final Supervised supervised) {
    supervised.stop(strategy.scope());
    result.access.writeUsing("informedCount", 1);
  }

  @Override
  public SupervisionStrategy supervisionStrategy() {
    return strategy;
  }

  public static class StopAllSupervisorResult {
    public AccessSafely access = AccessSafely.afterCompleting(0);

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
