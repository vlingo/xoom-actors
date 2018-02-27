// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class DeadLetters__Proxy implements DeadLetters {
  private final Actor actor;
  private final Mailbox mailbox;

  public DeadLetters__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public boolean isStopped() {
    return actor.isStopped();
  }

  @Override
  public void stop() {
    if (!actor.isStopped()) {
      final Consumer<DeadLetters> consumer = (actor) -> actor.stop();
      mailbox.send(new LocalMessage<DeadLetters>(actor, DeadLetters.class, consumer, "stop()"));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, "stop()"));
    }
  }

  @Override
  public void failedDelivery(final DeadLetter deadLetter) {
    if (!actor.isStopped()) {
      final Consumer<DeadLetters> consumer = (actor) -> actor.failedDelivery(deadLetter);
      mailbox.send(new LocalMessage<DeadLetters>(actor, DeadLetters.class, consumer, "failedDelivery(DeadLetter)"));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, "failedDelivery(DeadLetter)"));
    }
  }

  @Override
  public void registerListener(final DeadLettersListener listener) {
    if (!actor.isStopped()) {
      final Consumer<DeadLetters> consumer = (actor) -> actor.registerListener(listener);
      mailbox.send(new LocalMessage<DeadLetters>(actor, DeadLetters.class, consumer, "registerListener(DeadLettersListener)"));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, "registerListener(DeadLettersListener)"));
    }
  }
}
