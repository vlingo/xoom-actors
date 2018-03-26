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
  public static FailureControlActor instance;
  
  public AtomicInteger afterFailureCount = new AtomicInteger(0);
  public AtomicInteger afterFailureCountCount = new AtomicInteger(0);
  public AtomicInteger afterRestartCount = new AtomicInteger(0);
  public AtomicInteger afterStopCount = new AtomicInteger(0);
  public AtomicInteger beforeRestartCount = new AtomicInteger(0);
  public AtomicInteger beforeResume = new AtomicInteger(0);
  public AtomicInteger beforeStartCount = new AtomicInteger(0);
  public AtomicInteger failNowCount = new AtomicInteger(0);
  public AtomicInteger stoppedCount = new AtomicInteger(0);
  
  public TestUntil untilAfterFail;
  public TestUntil untilAfterRestart;
  public TestUntil untilBeforeResume;
  public TestUntil untilFailNow;
  public TestUntil untilFailureCount;
  public TestUntil untilStopped;
  
  public FailureControlActor() {
    instance = this;
  }
  
  @Override
  public void failNow() {
    failNowCount.incrementAndGet();
    if (untilFailNow != null) untilFailNow.happened();
    throw new IllegalStateException("Intended failure.");
  }

  @Override
  public void afterFailure() {
    afterFailureCount.incrementAndGet();
    if (untilAfterFail != null) untilAfterFail.happened();
  }

  @Override
  public void afterFailureCount(int count) {
    afterFailureCountCount.incrementAndGet();
    if (untilFailureCount != null) untilFailureCount.happened();
  }

  @Override
  protected void beforeStart() {
    beforeStartCount.incrementAndGet();
    if (untilFailNow != null) untilFailNow.happened();
    super.beforeStart();
  }

  @Override
  protected void afterStop() {
    afterStopCount.incrementAndGet();
    if (untilFailNow != null) untilFailNow.happened();
    super.afterStop();
  }

  @Override
  protected void beforeRestart(Throwable reason) {
    beforeRestartCount.incrementAndGet();
    if (untilFailNow != null) untilFailNow.happened();
    super.beforeRestart(reason);
  }

  @Override
  protected void afterRestart(Throwable reason) {
    super.afterRestart(reason);
    afterRestartCount.incrementAndGet();
    if (untilAfterRestart != null) untilAfterRestart.happened();
  }

  @Override
  protected void beforeResume(Throwable reason) {
    beforeResume.incrementAndGet();
    if (untilBeforeResume != null) untilBeforeResume.happened();
    super.beforeResume(reason);
  }

  @Override
  public void stop() {
    stoppedCount.incrementAndGet();
    if (untilStopped != null) untilStopped.happened();
    super.stop();
  }
}
