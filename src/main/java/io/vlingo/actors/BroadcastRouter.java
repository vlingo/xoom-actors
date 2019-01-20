// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.function.BiFunction;

import io.vlingo.common.Completes;
import io.vlingo.common.PentaFunction;
import io.vlingo.common.QuadFunction;
import io.vlingo.common.TriFunction;

/**
 * BroadcastRouter
 */
public abstract class BroadcastRouter<P> extends Router<P> {
  
  protected BroadcastRouter(final RouterSpecification<P> specification) {
    super(specification);
  }

  /* @see io.vlingo.actors.Router#computeRouting() */
  @Override
  protected Routing<P> computeRouting() {
    return Routing.with(routees());
  }

  /* @see io.vlingo.actors.Router#routeQuery(java.util.function.BiFunction, java.lang.Object) */
  @Override
  protected <T1, R extends Completes<?>> R routeQuery(BiFunction<P, T1, R> query, T1 routable1) {
    throw new UnsupportedOperationException("query protocols are not supported by this router by default");
  }

  /* @see io.vlingo.actors.Router#routeQuery(io.vlingo.common.TriFunction, java.lang.Object, java.lang.Object) */
  @Override
  protected <T1, T2, R extends Completes<?>> R routeQuery(TriFunction<P, T1, T2, R> query, T1 routable1, T2 routable2) {
    throw new UnsupportedOperationException("query protocols are not supported by this router by default");
  }

  /* @see io.vlingo.actors.Router#routeQuery(io.vlingo.common.QuadFunction, java.lang.Object, java.lang.Object, java.lang.Object) */
  @Override
  protected <T1, T2, T3, R extends Completes<?>> R routeQuery(QuadFunction<P, T1, T2, T3, R> query, T1 routable1,
          T2 routable2, T3 routable3) {
    throw new UnsupportedOperationException("query protocols are not supported by this router by default");
  }

  /* @see io.vlingo.actors.Router#routeQuery(io.vlingo.common.PentaFunction, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object) */
  @Override
  protected <T1, T2, T3, T4, R extends Completes<?>> R routeQuery(PentaFunction<P, T1, T2, T3, T4, R> query,
          T1 routable1, T2 routable2, T3 routable3, T4 routable4) {
    throw new UnsupportedOperationException("query protocols are not supported by this router by default");
  }
}
