// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.concurrent.CompletableFuture;

public class CompletesEventuallyActor extends Actor implements CompletesEventually {
  public CompletesEventuallyActor() { }

  @Override
  public void with(final Object outcome) {
    try {
      final PooledCompletes pooled = (PooledCompletes) outcome;
      if(pooled.clientReturns.asCompletes() != null) {
        pooled.clientReturns.asCompletes().with(pooled.outcome());
      }
      if(pooled.clientReturns.asCompletableFuture() != null) {
        pooled.clientReturns.asCompletableFuture().complete(pooled.outcome());
      }
      if(pooled.clientReturns.asFuture() != null) {
        ((CompletableFuture) pooled.clientReturns.asFuture()).complete(pooled.outcome());
      }
      } catch (Throwable t) {
      logger().error("The eventually completed outcome failed in the client because: " + t.getMessage(), t);
    }
  }
}
