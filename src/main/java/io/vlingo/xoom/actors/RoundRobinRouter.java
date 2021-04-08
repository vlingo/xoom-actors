// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.actors;

import java.util.List;
/**
 * RoundRobinRouter
 */
public class RoundRobinRouter<P> extends Router<P> {
  
  protected int poolIndex;
  
  public RoundRobinRouter(final RouterSpecification<P> specification) {
    super(specification);
  }
  
  int poolIndex() {
    return poolIndex;
  }
  
  /* @see io.vlingo.xoom.actors.Router#computeRouting() */
  @Override
  protected Routing<P> computeRouting() {
    return Routing.with(nextRoutee());
  }

  protected Routee<P> nextRoutee() {
    final List<Routee<P>> routees = routees();
    return routees.get(incrementAndGetPoolIndex() % routees.size());
  }
  
  private int incrementAndGetPoolIndex() {
    poolIndex = (poolIndex == Integer.MAX_VALUE) ? 0 : poolIndex + 1;
    return poolIndex;
  }
}
