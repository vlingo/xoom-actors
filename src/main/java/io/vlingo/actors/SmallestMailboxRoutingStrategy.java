// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.List;
/**
 * SmallestMailboxRoutingStrategy is a {@link RoutingStrategy} that
 * includes the pooled {@link Routee} with the fewest pending messages
 * in its {@link Mailbox} in the {@link Routing}. By default, the
 * first {@link Routee} encountered that has zero pendign messages will
 * be chosen. Otherwise, the {@link Routee} with the fewest pending
 * messages will be chosen.
 */
public class SmallestMailboxRoutingStrategy extends RoutingStrategyAdapter {
  
  @Override
  protected Routing chooseRouteFor(final List<Routee> routees) {
    Routee least = null;
    int leastCount = Integer.MAX_VALUE;
    for (Routee routee : routees) {
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
    return least == null ? Routing.empty() : Routing.with(least);
  }
}
