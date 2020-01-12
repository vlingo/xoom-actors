// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.actors.Actor;
import io.vlingo.actors.testkit.AccessSafely;

public class PongActor extends Actor implements Pong {
  public static final ThreadLocal<PongActor> instance = new ThreadLocal<>();

  private final PongTestResults testResults;

  public PongActor(final PongTestResults testResults) {
    this.testResults = testResults;
    instance.set(this);
  }

  @Override
  public void pong() {
    testResults.access.writeUsing("pongCount", 1);
    throw new IllegalStateException("Intended Pong failure.");
  }

  @Override
  public void stop() {
    super.stop();
    testResults.access.writeUsing("stopCount", 1);
  }

  public static class PongTestResults {
    public AccessSafely access = AccessSafely.afterCompleting(0);

    public final AtomicInteger pongCount = new AtomicInteger(0);
    public final AtomicInteger stopCount = new AtomicInteger(0);

    public AccessSafely afterCompleting(final int times) {
      access =
        AccessSafely.afterCompleting(times)
        .writingWith("pongCount", (Integer increment) -> pongCount.incrementAndGet())
        .readingWith("pongCount", () -> pongCount.get())

        .writingWith("stopCount", (Integer increment) -> stopCount.incrementAndGet())
        .readingWith("stopCount", () -> stopCount.get());

      return access;
    }
  }
}
