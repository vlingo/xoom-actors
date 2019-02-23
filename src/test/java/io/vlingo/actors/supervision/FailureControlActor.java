// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.actors.Actor;
import io.vlingo.actors.testkit.AccessSafely;

public class FailureControlActor extends Actor implements FailureControl {
  public static final ThreadLocal<FailureControlActor> instance = new ThreadLocal<>();
  
  private final FailureControlTestResults testResults;
  
  public FailureControlActor(final FailureControlTestResults testResults) {
    this.testResults = testResults;
    instance.set(this);
    this.testResults.access = this.testResults.afterCompleting(0);
  }
  
  @Override
  public void failNow() {
    testResults.access.writeUsing("failNowCount", 1);
    throw new IllegalStateException("Intended failure.");
  }

  @Override
  public void afterFailure() {
    testResults.access.writeUsing("afterFailureCount", 1);
  }

  @Override
  public void afterFailureCount(int count) {
    testResults.access.writeUsing("afterFailureCountCount", 1);
  }

  @Override
  protected void beforeStart() {
    testResults.access.writeUsing("beforeStartCount", 1);
    super.beforeStart();
  }

  @Override
  protected void afterStop() {
    testResults.access.writeUsing("afterStopCount", 1);
    super.afterStop();
  }

  @Override
  protected void beforeRestart(Throwable reason) {
    testResults.access.writeUsing("beforeRestartCount", 1);
    super.beforeRestart(reason);
  }

  @Override
  protected void afterRestart(Throwable reason) {
    super.afterRestart(reason);
    testResults.access.writeUsing("afterRestartCount", 1);
  }

  @Override
  protected void beforeResume(Throwable reason) {
    testResults.access.writeUsing("beforeResume", 1);
    super.beforeResume(reason);
  }

  @Override
  public void stop() {
    testResults.access.writeUsing("stoppedCount", 1);
    super.stop();
  }
  
  public static class FailureControlTestResults {
    public AccessSafely access = afterCompleting(0);

    public AtomicInteger afterFailureCount = new AtomicInteger(0);
    public AtomicInteger afterFailureCountCount = new AtomicInteger(0);
    public AtomicInteger afterRestartCount = new AtomicInteger(0);
    public AtomicInteger afterStopCount = new AtomicInteger(0);
    public AtomicInteger beforeRestartCount = new AtomicInteger(0);
    public AtomicInteger beforeResume = new AtomicInteger(0);
    public AtomicInteger beforeStartCount = new AtomicInteger(0);
    public AtomicInteger failNowCount = new AtomicInteger(0);
    public AtomicInteger stoppedCount = new AtomicInteger(0);

    public AccessSafely afterCompleting(final int times) {
      access =
        AccessSafely.afterCompleting(times)
        .writingWith("afterFailureCount", (Integer increment) -> afterFailureCount.set(afterFailureCount.get() + increment))
        .readingWith("afterFailureCount", () -> afterFailureCount.get())

        .writingWith("afterFailureCountCount", (Integer increment) -> afterFailureCountCount.set(afterFailureCountCount.get() + increment))
        .readingWith("afterFailureCountCount", () -> afterFailureCountCount.get())

        .writingWith("afterRestartCount", (Integer increment) -> afterRestartCount.set(afterRestartCount.get() + increment))
        .readingWith("afterRestartCount", () -> afterRestartCount.get())

        .writingWith("afterStopCount", (Integer increment) -> afterStopCount.set(afterStopCount.get() + increment))
        .readingWith("afterStopCount", () -> afterStopCount.get())

        .writingWith("beforeRestartCount", (Integer increment) -> beforeRestartCount.set(beforeRestartCount.get() + increment))
        .readingWith("beforeRestartCount", () -> beforeRestartCount.get())

        .writingWith("beforeResume", (Integer increment) -> beforeResume.set(beforeResume.get() + increment))
        .readingWith("beforeResume", () -> beforeResume.get())

        .writingWith("beforeStartCount", (Integer increment) -> beforeStartCount.set(beforeStartCount.get() + increment))
        .readingWith("beforeStartCount", () -> beforeStartCount.get())

        .writingWith("failNowCount", (Integer increment) -> failNowCount.set(failNowCount.get() + increment))
        .readingWith("failNowCount", () -> failNowCount.get())

        .writingWith("stoppedCount", (Integer increment) -> stoppedCount.set(stoppedCount.get() + increment))
        .readingWith("stoppedCount", () -> stoppedCount.get());

      return access;
    }
  }
}
