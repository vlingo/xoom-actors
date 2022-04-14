// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public interface Supervisor {
  void inform(final Throwable throwable, final Supervised supervised);
  SupervisionStrategy supervisionStrategy();
  
  /**
   * Answer my Supervisor; that is, this Supervisor's Supervisor (e.g. the escalation Supervisor).
   * Must override this default method to get a unique Supervisor of this Supervisor.
   * @return Supervisor
   */
  default Supervisor supervisor() {
    return new Supervisor() {
      @Override
      public void inform(final Throwable throwable, final Supervised supervised) {
        final SupervisionStrategy strategy = DefaultSupervisor.DefaultSupervisionStrategy;
        supervised.restartWithin(strategy.period(), strategy.intensity(), strategy.scope());
      }

      @Override
      public SupervisionStrategy supervisionStrategy() {
        return DefaultSupervisor.DefaultSupervisionStrategy;
      }
    };
  }
}
