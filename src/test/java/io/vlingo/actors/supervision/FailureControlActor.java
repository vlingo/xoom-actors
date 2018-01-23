// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import io.vlingo.actors.Actor;

public class FailureControlActor extends Actor implements FailureControl {
  public static volatile int afterFailureCount;
  public static volatile int afterFailureCountCount;
  public static volatile int afterRestartCount;
  public static volatile int afterStopCount;
  public static volatile int beforeRestartCount;
  public static volatile int beforeStartCount;
  public static volatile int failNowCount;
  public static volatile int stoppedCount;
  
  public static FailureControlActor instance;
  
  public FailureControlActor() {
    instance = this;
  }
  
  @Override
  public void failNow() {
    ++failNowCount;
    throw new IllegalStateException("Intended failure.");
  }

  @Override
  public void afterFailure() {
    ++afterFailureCount;
  }

  @Override
  public void afterFailureCount(int count) {
    ++afterFailureCountCount;
  }

  @Override
  protected void beforeStart() {
    ++beforeStartCount;
    super.beforeStart();
  }

  @Override
  protected void afterStop() {
    ++afterStopCount;
    super.afterStop();
  }

  @Override
  protected void beforeRestart(Throwable reason) {
    ++beforeRestartCount;
    super.beforeRestart(reason);
  }

  @Override
  protected void afterRestart(Throwable reason) {
    ++afterRestartCount;
    super.afterRestart(reason);
  }

  @Override
  public void stop() {
    ++stoppedCount;
    super.stop();
  }
}
