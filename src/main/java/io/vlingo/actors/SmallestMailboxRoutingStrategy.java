// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.List;
/**
 * SmallestMailboxRoutingStrategy
 */
public class SmallestMailboxRoutingStrategy implements RoutingStrategy {
  
  /* @see io.vlingo.actors.RoutingStrategy#chooseRouteFor(java.lang.Object, java.util.List) */
  @Override
  public <T> Routing chooseRouteFor(T routable, List<Routee> routees) {
    Routee least = null;
    int leastCount = Integer.MAX_VALUE;
    for (Routee routee : routees) {
      int count = routee.mailboxSize();
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
