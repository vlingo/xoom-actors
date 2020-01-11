// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public abstract class DefaultSupervisor extends Actor implements Supervisor {
  protected static final SupervisionStrategy DefaultSupervisionStrategy =
    new SupervisionStrategy() {
      @Override
      public int intensity() {
        return SupervisionStrategy.DefaultIntensity;
      }

      @Override
      public long period() {
        return SupervisionStrategy.DefaultPeriod;
      }

      @Override
      public Scope scope() {
        return Scope.One;
      }
    };

  @Override
  public void inform(final Throwable throwable, final Supervised supervised) {
    logger().error("DefaultSupervisor: Failure of: " + supervised.address() + " because: " + throwable.getMessage() + " Action: Possibly restarting.", throwable);
    supervised.restartWithin(DefaultSupervisionStrategy.period(), DefaultSupervisionStrategy.intensity(), DefaultSupervisionStrategy.scope());
  }

  @Override
  public SupervisionStrategy supervisionStrategy() {
    return DefaultSupervisionStrategy;
  }

  protected DefaultSupervisor() { }
}
