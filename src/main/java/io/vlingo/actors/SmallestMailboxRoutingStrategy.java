/* Copyright (c) 2005-2018 - Blue River Systems Group, LLC - All Rights Reserved */
package io.vlingo.actors;

import java.util.List;
/**
 * SmallestMailboxRoutingStrategy
 *
 * @author davem
 * @since Oct 27, 2018
 */
public class SmallestMailboxRoutingStrategy<T> implements RoutingStrategy<T> {
  
  /* @see io.vlingo.actors.RoutingStrategy#chooseRouteeFor(java.lang.Object, java.util.List) */
  @SuppressWarnings("unchecked")
  @Override
  public <R> Routing<T> chooseRouteFor(R routable, List<T> routees) {
    int leastSize = Integer.MAX_VALUE;
    Actor chosen = null;
    for (T routee : routees) {
      
      /* unfortunate need to cast from T to Actor */
      Actor actor = (Actor) routee;
      int msgCount = actor.lifeCycle.environment.mailbox.pendingMessages();
      
      /* 
       * This implementation simply considers current mailbox size, but should
       * be enhanced to consider whether the actor is busy, stopped, etc.
       */
      
      /* zero is the smallest mailbox possible, so stop searching */
      if (msgCount == 0) {
        chosen = actor;
        break;
      }
      
      /* remember the smallest mailbox seen so far */
      else if (msgCount < leastSize) {
        leastSize = msgCount;
        chosen = actor;
      }
    }
    
    /* unfortunate need to cast from Actor to T */
    return Routing.with((T) chosen);
  }
}
