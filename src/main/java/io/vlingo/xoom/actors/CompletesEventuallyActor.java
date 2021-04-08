// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.concurrent.CompletableFuture;

public class CompletesEventuallyActor extends Actor implements CompletesEventually {
  public CompletesEventuallyActor() { }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void with(final Object outcome) {
    try {
      final PooledCompletes pooled = (PooledCompletes) outcome;
      if (pooled.clientReturns.isCompletes()) {
        pooled.clientReturns.asCompletes().with(pooled.outcome());
      } else if (pooled.clientReturns.isCompletableFuture()) {
        pooled.clientReturns.asCompletableFuture().complete(pooled.outcome());
      } else if (pooled.clientReturns.isFuture()) {
        ((CompletableFuture) pooled.clientReturns.asFuture()).complete(pooled.outcome());
      }
    } catch (Throwable t) {
      logger().error("The eventually completed outcome failed in the client because: " + t.getMessage(), t);
    }
  }
}
