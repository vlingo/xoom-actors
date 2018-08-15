// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.ArrayList;
import java.util.List;

public class DeadLettersActor extends Actor implements DeadLetters {
  private final List<DeadLettersListener> listeners;

  public DeadLettersActor() {
    this.listeners = new ArrayList<DeadLettersListener>();
    
    stage().world().setDeadLetters(selfAs(DeadLetters.class));
  }

  public void failedDelivery(final DeadLetter deadLetter) {
    logger().log(deadLetter.toString());

    for (final DeadLettersListener listener : listeners) {
      try {
        listener.handle(deadLetter);
      } catch (Throwable t) {
        // ignore, but log
        logger().log("DeadLetters listener failed to handle: " + deadLetter, t);
      }
    }
  }

  @Override
  public void registerListener(final DeadLettersListener listener) {
    listeners.add(listener);
  }

  @Override
  protected void afterStop() {
    stage().world().setDeadLetters(null);
    super.afterStop();
  }
}
