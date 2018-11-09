// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.List;
/**
 * Router
 */
public abstract class Router extends Actor {
  
  private final List<Routee> routees;
  private final RoutingStrategy routingStrategy;
  
  protected Router(final RouterSpecification specification, final RoutingStrategy routingStrategy) {
    for (int i = 0; i < specification.poolSize(); i++) {
      childActorFor(specification.routerDefinition(), specification.routerProtocol());
    }
    this.routees = Routee.forAll(lifeCycle.environment.children);
    this.routingStrategy = routingStrategy;
  }
  
  protected <T> Routing computeRouting(T routable) {
    return routingStrategy.chooseRouteFor(routable, routees);
  }
}
