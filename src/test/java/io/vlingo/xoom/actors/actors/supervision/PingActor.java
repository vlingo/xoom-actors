// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.supervision;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.testkit.AccessSafely;

public class PingActor extends Actor implements Ping {
  public static final ThreadLocal<PingActor> instance = new ThreadLocal<>();

  private final PingTestResults testResults;

  public PingActor(final PingTestResults testResults) {
    this.testResults = testResults;
    instance.set(this);
  }

  @Override
  public void stop() {
    super.stop();
    testResults.access.writeUsing("stopCount", 1);
  }

  @Override
  public void ping() {
    testResults.access.writeUsing("pingCount", 1);
    throw new IllegalStateException("Intended Ping failure.");
  }

  public static class PingTestResults {
    public AccessSafely access = AccessSafely.afterCompleting(0);

    public final AtomicInteger pingCount = new AtomicInteger(0);
    public final AtomicInteger stopCount = new AtomicInteger(0);

    public AccessSafely afterCompleting(final int times) {
      access =
        AccessSafely.afterCompleting(times)
        .writingWith("pingCount", (Integer increment) -> pingCount.incrementAndGet())
        .readingWith("pingCount", () -> pingCount.get())

        .writingWith("stopCount", (Integer increment) -> stopCount.incrementAndGet())
        .readingWith("stopCount", () -> stopCount.get());

      return access;
    }
  }
}
