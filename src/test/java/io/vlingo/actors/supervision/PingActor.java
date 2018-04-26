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
    testResults.untilStopped.happened();
  }

  @Override
  public void ping() {
    testResults.pingCount.incrementAndGet();
    testResults.untilPinged.happened();
    throw new IllegalStateException("Intended Ping failure.");
  }

  public static class PingTestResults {
    public final AtomicInteger pingCount = new AtomicInteger(0);
    public TestUntil untilPinged = TestUntil.happenings(0);
    public TestUntil untilStopped = TestUntil.happenings(0);
  }
}
