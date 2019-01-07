// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.Collections;
import java.util.List;
/**
 * Router is a kind of {@link Actor} that forwards a message
 * to zero or more other {@link Actor actors} according to a
 * {@link Routing} that is computed by a {@link RoutingStrategy}.
 */
public abstract class Router extends Actor {
  
  //TODO: remove routees if their Actor is stopped
  //TODO: allow pool of routees to be dynamic / resizable
  
  private final List<Routee> routees;
  private final RoutingStrategy routingStrategy;
  
  protected Router(final RouterSpecification specification, final RoutingStrategy routingStrategy) {
    for (int i = 0; i < specification.poolSize(); i++) {
      childActorFor(specification.routerProtocol(), specification.routerDefinition());
    }
    this.routees = Routee.forAll(lifeCycle.environment.children);
    this.routingStrategy = routingStrategy;
  }
  
  protected <T1> Routing computeRouting(final T1 routable1) {
    Routing routing = routingStrategy.chooseRouteFor(routable1, routees);
    routing.validate();
    return routing;
  }
  
  protected <T1, T2> Routing computeRouting(final T1 routable1, final T2 routable2) {
    Routing routing = routingStrategy.chooseRouteFor(routable1, routable2, routees);
    routing.validate();
    return routing;
  }
  
  protected <T1, T2, T3> Routing computeRouting(final T1 routable1, final T2 routable2, final T3 routable3) {
    Routing routing = routingStrategy.chooseRouteFor(routable1, routable2, routable3, routees);
    routing.validate();
    return routing;
  }
  
  protected <T1, T2, T3, T4> Routing computeRouting(final T1 routable1, final T2 routable2, final T3 routable3, final T4 routable4) {
    Routing routing = routingStrategy.chooseRouteFor(routable1, routable2, routable3, routable4, routees);
    routing.validate();
    return routing;
  }
  
  protected <T1, T2, T3, T4, T5> Routing computeRouting(final T1 routable1, final T2 routable2, final T3 routable3, final T4 routable4, final T5 routable5) {
    Routing routing = routingStrategy.chooseRouteFor(routable1, routable2, routable3, routable4, routable5, routees);
    routing.validate();
    return routing;
  }
  
  protected List<Routee> routees() {
    return Collections.unmodifiableList(routees);
  }
}
