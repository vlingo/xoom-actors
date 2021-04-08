// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.supervision;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Supervised;
import io.vlingo.xoom.actors.SupervisionStrategy;
import io.vlingo.xoom.actors.Supervisor;

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
    logger().error("DefaultSupervisorOverride: Failure of: " + supervised.address() + " because: " + throwable.getMessage() + " Action: Resuming.", throwable);
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
