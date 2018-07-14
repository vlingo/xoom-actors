// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.supervision;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Supervised;
import io.vlingo.actors.SupervisionStrategy;
import io.vlingo.actors.Supervisor;

public class DefaultSupervisorOverride extends Actor implements Supervisor {
  private final Supervisor parentSupervisor;
  
  private final SupervisionStrategy supervisionStrategy =
          new SupervisionStrategy() {
            @Override
            public int intensity() {
              return ForeverIntensity;
            }

            @Override
            public long period() {
              return ForeverPeriod;
            }

            @Override
            public Scope scope() {
              return Scope.One;
            }
          };

  public DefaultSupervisorOverride() {
    this.parentSupervisor = parentAs(Supervisor.class);
  }

  @Override
  public void inform(final Throwable throwable, final Supervised supervised) {
    logger().log("DefaultSupervisorOverride: Failure of: " + supervised.address(), throwable);
    supervised.resume();
  }

  @Override
  public SupervisionStrategy supervisionStrategy() {
    return supervisionStrategy;
  }

  @Override
  public Supervisor supervisor() {
    return parentSupervisor;
  }
}
