// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Supervised;
import io.vlingo.actors.SupervisionStrategy;
import io.vlingo.actors.Supervisor;

public class SuspendedSenderSupervisorActor extends Actor implements Supervisor, FailureControlSender {
  public static volatile int informedCount;
  public static SuspendedSenderSupervisorActor instance;
  
  private FailureControl failureControl;
  private int times;
  
  public SuspendedSenderSupervisorActor() {
    instance = this;
  }
  
  private final SupervisionStrategy strategy =
          new SupervisionStrategy() {
            @Override
            public int intensity() {
              return SupervisionStrategy.ForeverIntensity;
            }

            @Override
            public long period() {
              return SupervisionStrategy.ForeverPeriod;
            }

            @Override
            public Scope scope() {
              return Scope.One;
            }
          };
  
  @Override
  public void inform(final Throwable throwable, final Supervised supervised) {
    ++informedCount;
    
    for (int idx = 1; idx <= times; ++idx) {
      failureControl.afterFailureCount(idx);
    }
    try { Thread.sleep(100); } catch (Exception e) {}
    supervised.resume();
  }

  @Override
  public SupervisionStrategy supervisionStrategy() {
    return strategy;
  }

  @Override
  public void sendUsing(final FailureControl failureControl, final int times) {
    this.failureControl = failureControl;
    this.times = times;
  }
}
