// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public class CompletesEventuallyActor extends Actor implements CompletesEventually {
  public CompletesEventuallyActor() { }

  @Override
  public void with(final Object outcome) {
    try {
      final CompletesHolder holder = (CompletesHolder) outcome;
      holder.clientCompletes.with(holder.outcome());
    } catch (Throwable t) {
      logger().log("The eventually completed outcome failed in the client because: " + t.getMessage(), t);
    }
  }
}
