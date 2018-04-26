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

public class FailureControlActor extends Actor implements FailureControl {
  public static final ThreadLocal<FailureControlActor> instance = new ThreadLocal<>();
  
  private final FailureControlTestResults testResults;
  
  public FailureControlActor(final FailureControlTestResults testResults) {
    this.testResults = testResults;
    instance.set(this);
  }
  
  @Override
  public void failNow() {
    testResults.failNowCount.incrementAndGet();
    testResults.untilFailNow.happened();
    throw new IllegalStateException("Intended failure.");
  }

  @Override
  public void afterFailure() {
    testResults.afterFailureCount.incrementAndGet();
    testResults.untilAfterFail.happened();
  }

  @Override
  public void afterFailureCount(int count) {
    testResults.afterFailureCountCount.incrementAndGet();
    testResults.untilFailureCount.happened();
  }

  @Override
  protected void beforeStart() {
    testResults.beforeStartCount.incrementAndGet();
    testResults.untilFailNow.happened();
    super.beforeStart();
  }

  @Override
  protected void afterStop() {
    testResults.afterStopCount.incrementAndGet();
    testResults.untilFailNow.happened();
    super.afterStop();
  }

  @Override
  protected void beforeRestart(Throwable reason) {
    testResults.beforeRestartCount.incrementAndGet();
    testResults.untilFailNow.happened();
    super.beforeRestart(reason);
  }

  @Override
  protected void afterRestart(Throwable reason) {
    super.afterRestart(reason);
    testResults.afterRestartCount.incrementAndGet();
    testResults.untilAfterRestart.happened();
  }

  @Override
  protected void beforeResume(Throwable reason) {
    testResults.beforeResume.incrementAndGet();
    testResults.untilBeforeResume.happened();
    super.beforeResume(reason);
  }

  @Override
  public void stop() {
    testResults.stoppedCount.incrementAndGet();
    testResults.untilStopped.happened();
    super.stop();
  }
  
  public static class FailureControlTestResults {
    public AtomicInteger afterFailureCount = new AtomicInteger(0);
    public AtomicInteger afterFailureCountCount = new AtomicInteger(0);
    public AtomicInteger afterRestartCount = new AtomicInteger(0);
    public AtomicInteger afterStopCount = new AtomicInteger(0);
    public AtomicInteger beforeRestartCount = new AtomicInteger(0);
    public AtomicInteger beforeResume = new AtomicInteger(0);
    public AtomicInteger beforeStartCount = new AtomicInteger(0);
    public AtomicInteger failNowCount = new AtomicInteger(0);
    public AtomicInteger stoppedCount = new AtomicInteger(0);
    
    public TestUntil untilAfterFail = TestUntil.happenings(0);
    public TestUntil untilAfterRestart = TestUntil.happenings(0);
    public TestUntil untilBeforeResume = TestUntil.happenings(0);
    public TestUntil untilFailNow = TestUntil.happenings(0);
    public TestUntil untilFailureCount = TestUntil.happenings(0);
    public TestUntil untilStopped = TestUntil.happenings(0);
  }
}
