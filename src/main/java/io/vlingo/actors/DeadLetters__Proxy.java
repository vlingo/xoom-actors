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
  private final DeadLetters typedActor;
  private final Mailbox mailbox;

  public DeadLetters__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.typedActor = (DeadLetters) actor;
    this.mailbox = mailbox;
  }

  @Override
  public boolean isStopped() {
    return actor.isStopped();
  }

  @Override
  public void stop() {
    final Consumer<DeadLetters> consumer = (actor) -> actor.stop();
    mailbox.send(new LocalMessage<DeadLetters>(actor, typedActor, consumer, "stop()"));
  }

  @Override
  public void failedDelivery(final DeadLetter deadLetter) {
    final Consumer<DeadLetters> consumer = (actor) -> actor.failedDelivery(deadLetter);
    mailbox.send(new LocalMessage<DeadLetters>(actor, typedActor, consumer, "failedDelivery(DeadLetter)"));
  }

  @Override
  public void registerListener(final DeadLettersListener listener) {
    final Consumer<DeadLetters> consumer = (actor) -> actor.registerListener(listener);
    mailbox.send(new LocalMessage<DeadLetters>(actor, typedActor, consumer, "registerListener(DeadLettersListener)"));
  }
}
