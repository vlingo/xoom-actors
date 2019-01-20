// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import io.vlingo.common.Completes;
import io.vlingo.common.PentaConsumer;
import io.vlingo.common.PentaFunction;
import io.vlingo.common.QuadConsumer;
import io.vlingo.common.QuadFunction;
import io.vlingo.common.TriConsumer;
import io.vlingo.common.TriFunction;
/**
 * Router is a kind of {@link Actor} that forwards a message
 * to one or more other {@link Actor actors} according to a
 * computed {@link Routing}.
 */
public abstract class Router<P> extends Actor {
  
  protected final RouteePool<P> pool;
  
  public Router(final RouterSpecification<P> specification) {
    this.pool = RouteePool.empty();
    initRouterPool(specification);
  }

  protected void initRouterPool(final RouterSpecification<P> specification) {
    for (int i = 0; i < specification.initialPoolSize(); i++) {
      P child = childActorFor(specification.routerProtocol(), specification.routerDefinition());
      pool.subscribe(Routee.of(child));
    }
  }
  
  protected List<Routee<P>> routees() {
    return pool.routees();
  }
  
  protected abstract Routing<P> computeRouting();
  
  protected <T1> Routing<P> routingFor(final T1 routable1) {
    /* by default, assume the routing is not dependent on message content */
    return computeRouting();
  }

  protected <T1, T2> Routing<P> routingFor(final T1 routable1, final T2 routable2) {
    /* by default, assume the routing is not dependent on message content */
    return computeRouting();
  }
  
  protected <T1, T2, T3> Routing<P> routingFor(final T1 routable1, final T2 routable2, final T3 routable3) {
    /* by default, assume the routing is not dependent on message content */
    return computeRouting();
  }

  protected <T1, T2, T3, T4> Routing<P> routingFor(final T1 routable1, final T2 routable2, final T3 routable3, final T4 routable4) {
    /* by default, assume the routing is not dependent on message content */
    return computeRouting();
  }
  
  protected <T1> void routeCommand(final BiConsumer<P, T1> action, final T1 routable1) {
    routingFor(routable1)
      .routees()
      .forEach(routee -> routee.receiveCommand(action, routable1));
  }
  
  protected <T1, T2> void routeCommand(final TriConsumer<P, T1, T2> action, final T1 routable1, final T2 routable2) {
    routingFor(routable1, routable2)
      .routees()
      .forEach(routee -> routee.receiveCommand(action, routable1, routable2));
  }
  
  protected <T1, T2, T3> void routeCommand(final QuadConsumer<P, T1, T2, T3> action, final T1 routable1, final T2 routable2, final T3 routable3) {
    routingFor(routable1, routable2, routable3)
      .routees()
      .forEach(routee -> routee.receiveCommand(action, routable1, routable2, routable3));
  }
  
  protected <T1, T2, T3, T4> void routeCommand(final PentaConsumer<P, T1, T2, T3, T4> action, final T1 routable1, final T2 routable2, final T3 routable3, final T4 routable4) {
    routingFor(routable1, routable2, routable3, routable4)
      .routees()
      .forEach(routee -> routee.receiveCommand(action, routable1, routable2, routable3, routable4));
  }

  @SuppressWarnings("unchecked")
  protected <T1, R extends Completes<?>> R routeQuery(final BiFunction<P, T1, R> query, final T1 routable1) {
    final CompletesEventually completesEventually = completesEventually();
    routingFor(routable1)
      .first() //by default, for protocols with a return value, route only to first routee
      .receiveQuery(query, routable1)
      .andThenConsume(outcome -> completesEventually.with(outcome));
    return (R) completes(); //this is a fake out; the real completes doesn't happen until inside the lambda
  }
  
  @SuppressWarnings("unchecked")
  protected <T1, T2, R extends Completes<?>> R routeQuery(final TriFunction<P, T1, T2, R> query, final T1 routable1, final T2 routable2) {
    final CompletesEventually completesEventually = completesEventually();
    routingFor(routable1, routable2)
      .first() //by default, for protocols with a return value, route only to first routee
      .receiveQuery(query, routable1, routable2)
      .andThenConsume(outcome -> completesEventually.with(outcome));
    return (R) completes(); //this is a fake out; the real completes doesn't happen until inside the lambda
  }

  @SuppressWarnings("unchecked")
  protected <T1, T2, T3, R extends Completes<?>> R routeQuery(final QuadFunction<P, T1, T2, T3, R> query, final T1 routable1, final T2 routable2, final T3 routable3) {
    final CompletesEventually completesEventually = completesEventually();
    routingFor(routable1, routable2)
      .first() //by default, for protocols with a return value, route only to first routee
      .receiveQuery(query, routable1, routable2, routable3)
      .andThenConsume(outcome -> completesEventually.with(outcome));
    return (R) completes(); //this is a fake out; the real completes doesn't happen until inside the lambda
  }
  
  @SuppressWarnings("unchecked")
  protected <T1, T2, T3, T4, R extends Completes<?>> R routeQuery(final PentaFunction<P, T1, T2, T3, T4, R> query, final T1 routable1, final T2 routable2, final T3 routable3, final T4 routable4) {
    final CompletesEventually completesEventually = completesEventually();
    routingFor(routable1, routable2)
      .first() //by default, for protocols with a return value, route only to first routee
      .receiveQuery(query, routable1, routable2, routable3, routable4)
      .andThenConsume(outcome -> completesEventually.with(outcome));
    return (R) completes(); //this is a fake out; the real completes doesn't happen until inside the lambda
  }
}
