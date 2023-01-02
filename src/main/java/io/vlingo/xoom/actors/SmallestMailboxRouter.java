// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;
/**
 * SmallestMailboxRouter
 */
public class SmallestMailboxRouter<P> extends Router<P> {

  public SmallestMailboxRouter(final RouterSpecification<P> specification) {
    super(specification);
  }

  /* @see io.vlingo.xoom.actors.Router#computeRouting() */
  @Override
  protected Routing<P> computeRouting() {
    Routee<P> least = null;
    int leastCount = Integer.MAX_VALUE;
    for (Routee<P> routee : routees()) {
      int count = routee.pendingMessages();
      if (count == 0) {
        least = routee;
        break;
      }
      else if (count < leastCount) {
        least = routee;
        leastCount = count;
      }
    }
    return Routing.with(least);
  }
}
