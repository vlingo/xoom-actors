// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.ArrayList;
import java.util.List;

public class DeadLettersActor extends Actor implements DeadLetters {
  private final List<DeadLettersListener> listeners;

  public DeadLettersActor() {
    this.listeners = new ArrayList<>();
  }

  public void failedDelivery(final DeadLetter deadLetter) {
    logger().debug(deadLetter.toString());

    for (final DeadLettersListener listener : listeners) {
      try {
        listener.handle(deadLetter);
      } catch (Throwable t) {
        // ignore, but log
        logger().warn("DeadLetters listener failed to handle: " + deadLetter, t);
      }
    }
  }

  @Override
  public void registerListener(final DeadLettersListener listener) {
    listeners.add(listener);
  }

  @Override
  protected void beforeStart() {
    super.beforeStart();

    stage().world().setDeadLetters(selfAs(DeadLetters.class));
  }

  @Override
  protected void afterStop() {
    stage().world().setDeadLetters(null);
    super.afterStop();
  }
}
