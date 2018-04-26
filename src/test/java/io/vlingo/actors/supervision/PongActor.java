// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.actors.Actor;
import io.vlingo.actors.testkit.TestUntil;

public class PongActor extends Actor implements Pong {
  public static final ThreadLocal<PongActor> instance = new ThreadLocal<>();
  
  private final PongTestResults testResults;
  
  public PongActor(final PongTestResults testResults) {
    this.testResults = testResults;
    instance.set(this);
  }
  
  @Override
  public void pong() {
    testResults.pongCount.incrementAndGet();
    testResults.untilPonged.happened();
    throw new IllegalStateException("Intended Pong failure.");
  }

  @Override
  public void stop() {
    super.stop();
    testResults.untilStopped.happened();
  }

  public static class PongTestResults {
    public AtomicInteger pongCount = new AtomicInteger(0);
    public TestUntil untilPonged = TestUntil.happenings(0);
    public TestUntil untilStopped = TestUntil.happenings(0);
  }
}
