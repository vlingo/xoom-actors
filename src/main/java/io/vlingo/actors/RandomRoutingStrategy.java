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
 * RandomRoutingStrategy
 */
public class RandomRoutingStrategy implements RoutingStrategy {
  
  private final Random random;
  
  public RandomRoutingStrategy() {
    super();
    this.random = new Random();
  }

  /* @see io.vlingo.actors.RoutingStrategy#chooseRouteFor(java.lang.Object, java.util.List) */
  @Override
  public <T> Routing chooseRouteFor(T routable, List<Routee> routees) {
    int index = random.nextInt(routees.size());
    return Routing.with(routees.get(index));
  }
}
