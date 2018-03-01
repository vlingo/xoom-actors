// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import io.vlingo.actors.Actor;
import io.vlingo.actors.testkit.TestUntil;

public class FailureControlActor extends Actor implements FailureControl {
  public static volatile int afterFailureCount;
  public static volatile int afterFailureCountCount;
  public static volatile int afterRestartCount;
  public static volatile int afterStopCount;
  public static volatile int beforeRestartCount;
  public static volatile int beforeResume;
  public static volatile int beforeStartCount;
  public static volatile int failNowCount;
  public static volatile int stoppedCount;
  
  public static FailureControlActor instance;
  
  public static TestUntil untilAfterFail;
  public static TestUntil untilAfterRestart;
  public static TestUntil untilBeforeResume;
  public static TestUntil untilFailNow;
  public static TestUntil untilStopped;
  
  public FailureControlActor() {
    instance = this;
  }
  
  @Override
  public void failNow() {
    ++failNowCount;
    if (untilFailNow != null) untilFailNow.happened();
    throw new IllegalStateException("Intended failure.");
  }

  @Override
  public void afterFailure() {
    ++afterFailureCount;
    if (untilAfterFail != null) untilAfterFail.happened();
  }

  @Override
  public void afterFailureCount(int count) {
    ++afterFailureCountCount;
    if (untilFailNow != null) untilFailNow.happened();
  }

  @Override
  protected void beforeStart() {
    ++beforeStartCount;
    if (untilFailNow != null) untilFailNow.happened();
    super.beforeStart();
  }

  @Override
  protected void afterStop() {
    ++afterStopCount;
    if (untilFailNow != null) untilFailNow.happened();
    super.afterStop();
  }

  @Override
  protected void beforeRestart(Throwable reason) {
    ++beforeRestartCount;
    if (untilFailNow != null) untilFailNow.happened();
    super.beforeRestart(reason);
  }

  @Override
  protected void afterRestart(Throwable reason) {
    super.afterRestart(reason);
    ++afterRestartCount;
    if (untilAfterRestart != null) untilAfterRestart.happened();
  }

  @Override
  protected void beforeResume(Throwable reason) {
    ++beforeResume;
    if (untilBeforeResume != null) untilBeforeResume.happened();
    super.beforeResume(reason);
  }

  @Override
  public void stop() {
    ++stoppedCount;
    if (untilStopped != null) untilStopped.happened();
    super.stop();
  }
}
