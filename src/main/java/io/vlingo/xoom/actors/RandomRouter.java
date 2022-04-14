// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.actors;

import java.util.Random;
/**
 * RandomRouter
 */
public class RandomRouter<P> extends Router<P> {
  
  private final Random random;
  
  public RandomRouter(final RouterSpecification<P> specification) {
    this(specification, new Random(System.currentTimeMillis()));
  }

  RandomRouter(final RouterSpecification<P> specification, final Random seededRandom) {
    super(specification);
    this.random = seededRandom;
  }

  /* @see io.vlingo.xoom.actors.Router#computeRouting() */
  @Override
  protected Routing<P> computeRouting() {
    return Routing.with(nextRoutee());
  }
  
  protected Routee<P> nextRoutee() {
    int index = random.nextInt(routees.size());
    return routeeAt(index);
  }
}
