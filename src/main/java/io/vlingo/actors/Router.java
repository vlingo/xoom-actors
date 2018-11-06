/* Copyright (c) 2005-2018 - Blue River Systems Group, LLC - All Rights Reserved */
package io.vlingo.actors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Router
 *
 * @author davem
 * @since Oct 26, 2018
 */
public abstract class Router<T> extends Actor {
  
  private final List<T> routees;
  private final RoutingStrategy<T> routingStrategy;
  
  protected Router(final RouterSpecification<T> specification, final RoutingStrategy<T> routingStrategy) {
    this.routees = new ArrayList<>();
    for (int i = 0; i < specification.poolSize(); i++) {
      T child = childActorFor(specification.routerDefinition(), specification.routerProtocol());
      routees.add(child);
    }
    this.routingStrategy = routingStrategy;
  }
  
  public List<T> routees() {
    return Collections.unmodifiableList(routees);
  }
  
  protected <R> Routing<T> computeRouting(R routable) {
    return routingStrategy.chooseRouteFor(routable, routees);
  }
}
