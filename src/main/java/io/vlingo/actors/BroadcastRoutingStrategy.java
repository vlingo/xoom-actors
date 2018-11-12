// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import java.util.List;
/**
 * BroadcastRoutingStrategy is a {@link RoutingStrategy} that
 * includes all pooled {@link Routee routees} in the {@link Routing}.
 */
public class BroadcastRoutingStrategy extends RoutingStrategyAdapter {

  public BroadcastRoutingStrategy() {
    super();
  }

  @Override
  public Routing chooseRouteFor(final List<Routee> routees) {
    return Routing.with(routees);
  }
}
