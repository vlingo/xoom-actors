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

public class PongSupervisor extends Actor implements Supervisor {
  public static int informedCount;
  
  public PongSupervisor() { }
  
  private final SupervisionStrategy strategy =
          new SupervisionStrategy() {
            @Override
            public int intensity() {
              return 10;
            }

            @Override
            public long period() {
              return 1000;
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
  }

  @Override
  public SupervisionStrategy supervisionStrategy() {
    return strategy;
  }
}
