// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.List;
import java.util.Random;
/**
 * RandomRoutingStrategy is a {@link RoutingStrategy} that
 * includes a random one of the pooled {@link Routee routees}
 * in the {@link Routing}
 */
public class RandomRoutingStrategy extends RoutingStrategyAdapter {
  
  private final Random random;
  
  public RandomRoutingStrategy() {
    super();
    this.random = new Random();
  }

  @Override
  protected Routing chooseRouteFor(final List<Routee> routees) {
    int index = random.nextInt(routees.size());
    return Routing.with(routees.get(index));
  }
}
