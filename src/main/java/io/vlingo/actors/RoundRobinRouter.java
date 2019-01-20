// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.List;
/**
 * RoundRobinRouter
 */
public class RoundRobinRouter<P> extends Router<P> {
  
  protected int poolIndex;
  
  public RoundRobinRouter(final RouterSpecification<P> specification) {
    super(specification);
  }
  
  /* @see io.vlingo.actors.Router#computeRouting() */
  @Override
  protected Routing<P> computeRouting() {
    return Routing.with(nextRoutee());
  }

  protected Routee<P> nextRoutee() {
    final List<Routee<P>> routees = routees();
    return routees.get(poolIndex++ % routees.size());
  }
}
