// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.actors;

import java.util.function.BiFunction;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.PentaFunction;
import io.vlingo.xoom.common.QuadFunction;
import io.vlingo.xoom.common.TriFunction;

/**
 * BroadcastRouter
 */
public abstract class BroadcastRouter<P> extends Router<P> {
  
  protected BroadcastRouter(final RouterSpecification<P> specification) {
    super(specification);
  }

  /* @see io.vlingo.xoom.actors.Router#computeRouting() */
  @Override
  protected Routing<P> computeRouting() {
    return Routing.with(routees());
  }

  /* @see io.vlingo.xoom.actors.Router#routeQuery(java.util.function.BiFunction, java.lang.Object) */
  @Override
  protected <T1, R extends Completes<?>> R dispatchQuery(BiFunction<P, T1, R> query, T1 routable1) {
    throw new UnsupportedOperationException("query protocols are not supported by this router by default");
  }

  /* @see io.vlingo.xoom.actors.Router#routeQuery(io.vlingo.xoom.common.TriFunction, java.lang.Object, java.lang.Object) */
  @Override
  protected <T1, T2, R extends Completes<?>> R dispatchQuery(TriFunction<P, T1, T2, R> query, T1 routable1, T2 routable2) {
    throw new UnsupportedOperationException("query protocols are not supported by this router by default");
  }

  /* @see io.vlingo.xoom.actors.Router#routeQuery(io.vlingo.xoom.common.QuadFunction, java.lang.Object, java.lang.Object, java.lang.Object) */
  @Override
  protected <T1, T2, T3, R extends Completes<?>> R dispatchQuery(QuadFunction<P, T1, T2, T3, R> query, T1 routable1,
          T2 routable2, T3 routable3) {
    throw new UnsupportedOperationException("query protocols are not supported by this router by default");
  }

  /* @see io.vlingo.xoom.actors.Router#routeQuery(io.vlingo.xoom.common.PentaFunction, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object) */
  @Override
  protected <T1, T2, T3, T4, R extends Completes<?>> R dispatchQuery(PentaFunction<P, T1, T2, T3, T4, R> query,
          T1 routable1, T2 routable2, T3 routable3, T4 routable4) {
    throw new UnsupportedOperationException("query protocols are not supported by this router by default");
  }
}
