// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public class PublicRootActor extends Actor implements Stoppable, Supervisor {
  private final Supervisor self;
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

  public PublicRootActor() {
      this.self = selfAs(Supervisor.class);
  }

    @Override
    protected void beforeStart() {
        super.beforeStart();

        stage().world().setDefaultParent(this);
        stage().world().setPublicRoot(selfAs(Stoppable.class));
    }

    @Override
  protected void afterStop() {
    stage().world().setDefaultParent(null);
    stage().world().setPublicRoot(null);
    super.afterStop();
  }

  @Override
  public void inform(final Throwable throwable, final Supervised supervised) {
    logger().error("PublicRootActor: Failure of: " + supervised.address() + " because: " + throwable.getMessage() + " Action: Restarting.", throwable);
    supervised.restartWithin(supervisionStrategy.period(), supervisionStrategy.intensity(), supervisionStrategy.scope());
  }

  @Override
  public SupervisionStrategy supervisionStrategy() {
    return supervisionStrategy;
  }

  @Override
  public Supervisor supervisor() {
    // this currently should never be invoked because I always restart() the Supervised.
    return self;
  }
}
