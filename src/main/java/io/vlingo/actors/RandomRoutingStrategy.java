/* Copyright (c) 2005-2018 - Blue River Systems Group, LLC - All Rights Reserved */
package io.vlingo.actors;

import java.util.List;
import java.util.Random;
/**
 * RandomRoutingStrategy
 *
 * @author davem
 * @since Oct 27, 2018
 */
public class RandomRoutingStrategy<T> implements RoutingStrategy<T> {
  
  private final Random random;
  
  public RandomRoutingStrategy() {
    super();
    random = new Random();
  }
  
  /* @see io.vlingo.actors.RoutingStrategy#chooseRouteeFor(java.lang.Object, java.util.List) */
  @Override
  public <R> Routing<T> chooseRouteFor(R routable, List<T> routees) {
    int idx = random.nextInt(routees.size());
    return Routing.with(routees.get(idx));
  }
}
