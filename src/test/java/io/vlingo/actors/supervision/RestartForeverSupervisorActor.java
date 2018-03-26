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
import io.vlingo.actors.testkit.TestUntil;

public class RestartForeverSupervisorActor extends Actor implements Supervisor {
  public static RestartForeverSupervisorActor instance;
  
  public int informedCount;
  public TestUntil untilInform;

  public RestartForeverSupervisorActor() {
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
    
    supervised.restartWithin(strategy.period(), strategy.intensity(), strategy.scope());
    
    untilInform.happened();
  }

  @Override
  public SupervisionStrategy supervisionStrategy() {
    return strategy;
  }
}
