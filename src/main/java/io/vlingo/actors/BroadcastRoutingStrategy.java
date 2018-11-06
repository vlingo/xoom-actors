/* Copyright (c) 2005-2018 - Blue River Systems Group, LLC - All Rights Reserved */
package io.vlingo.actors;

import java.util.List;
/**
 * BroadcastRoutingStrategy
 *
 * @author davem
 * @since Oct 29, 2018
 */
public class BroadcastRoutingStrategy<T> implements RoutingStrategy<T> {

  /* @see io.vlingo.actors.RoutingStrategy#chooseRouteFor(java.lang.Object, java.util.List) */
  @Override
  public <R> Routing<T> chooseRouteFor(R routable, List<T> routees) {
    return Routing.with(routees);
  }

}
