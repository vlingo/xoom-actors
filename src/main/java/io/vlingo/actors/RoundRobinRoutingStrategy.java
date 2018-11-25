// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.List;
/**
 * RoundRobinRoutingStrategy is a {@link RoutingStrategy} that
 * treats its pool of {@link Routee routees} as if it were a
 * circular linked list and which includes each routee, in turn,
 * in the {@link Routing}.
 */
public class RoundRobinRoutingStrategy extends RoutingStrategyAdapter {
  
  private int lastIndex;

  public RoundRobinRoutingStrategy() {
    super();
    lastIndex = 0;
  }
  
  @Override
  protected Routing chooseRouteFor(final List<Routee> routees) {
    final int nextIndex = lastIndex++ % routees.size();
    return Routing.with(routees.get(nextIndex));
  }
}
