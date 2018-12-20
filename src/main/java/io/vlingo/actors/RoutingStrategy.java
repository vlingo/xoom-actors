// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.List;
/**
 * RoutingStrategy is an object that knows how to compute a
 * {@link Routing} for a message based on a defined strategy
 * (e.g., round robin, smallest mailbox, etc.). An empty {@link Routing}
 * is not legal and will result in an {@link IllegalStateException}.
 */
public interface RoutingStrategy {
  <T1> Routing chooseRouteFor(final T1 routable1, final List<Routee> routees);
  <T1, T2> Routing chooseRouteFor(final T1 routable1, final T2 routable2, final List<Routee> routees);
  <T1, T2, T3> Routing chooseRouteFor(final T1 routable1, final T2 routable2, final T3 routable3, final List<Routee> routees);
  <T1, T2, T3, T4> Routing chooseRouteFor(final T1 routable1, final T2 routable2, final T3 routable3, final T4 routable4, final List<Routee> routees);
  <T1, T2, T3, T4, T5> Routing chooseRouteFor(final T1 routable1, final T2 routable2, final T3 routable3, final T4 routable4, final T5 routable5, final List<Routee> routees);
}
