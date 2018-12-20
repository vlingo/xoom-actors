// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.List;
/**
 * RoutingStrategyAdapter provides default implementations of the methods
 * declared by {@link RoutingStrategy}. 
 */
public abstract class RoutingStrategyAdapter implements RoutingStrategy {

  public RoutingStrategyAdapter() {
    super();
  }
  
  protected Routing chooseRouteFor(final List<Routee> routees) {
    throw new IllegalStateException(getClass().getName() + " must implement chooseRouteFor(List<Routeee>)");
  }

  /* @see io.vlingo.actors.RoutingStrategy#chooseRouteFor(java.lang.Object, java.util.List) */
  @Override
  public <T1> Routing chooseRouteFor(final T1 routable1, final List<Routee> routees) {
    return chooseRouteFor(routees);
  }

  /* @see io.vlingo.actors.RoutingStrategy#chooseRouteFor(java.lang.Object, java.lang.Object, java.util.List) */
  @Override
  public <T1, T2> Routing chooseRouteFor(final T1 routable1, final T2 routable2, final List<Routee> routees) {
    return chooseRouteFor(routees);
  }

  /* @see io.vlingo.actors.RoutingStrategy#chooseRouteFor(java.lang.Object, java.lang.Object, java.lang.Object, java.util.List) */
  @Override
  public <T1, T2, T3> Routing chooseRouteFor(final T1 routable1, final T2 routable2, final T3 routable3, final List<Routee> routees) {
    return chooseRouteFor(routees);
  }

  /* @see io.vlingo.actors.RoutingStrategy#chooseRouteFor(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.util.List) */
  @Override
  public <T1, T2, T3, T4> Routing chooseRouteFor(final T1 routable1, final T2 routable2, final T3 routable3, final T4 routable4, final List<Routee> routees) {
    return chooseRouteFor(routees);
  }

  /* @see io.vlingo.actors.RoutingStrategy#chooseRouteFor(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.util.List) */
  @Override
  public <T1, T2, T3, T4, T5> Routing chooseRouteFor(final T1 routable1, final T2 routable2, final T3 routable3, final T4 routable4, final T5 routable5, final List<Routee> routees) {
    return chooseRouteFor(routees);
  }
}
