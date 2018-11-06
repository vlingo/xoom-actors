/* Copyright (c) 2005-2018 - Blue River Systems Group, LLC - All Rights Reserved */
package io.vlingo.actors;

import java.util.List;
/**
 * RoutingStrategy
 *
 * @author davem
 * @since Oct 26, 2018
 */
public interface RoutingStrategy<T> {
  <R> Routing<T> chooseRouteFor(R routable, List<T> routees);
}
