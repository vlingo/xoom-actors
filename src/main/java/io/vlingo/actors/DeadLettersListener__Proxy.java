// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class DeadLettersListener__Proxy implements DeadLettersListener {
  private final Actor actor;
  private final DeadLettersListener typedActor;
  private final Mailbox mailbox;

  public DeadLettersListener__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.typedActor = (DeadLettersListener) actor;
    this.mailbox = mailbox;
  }

  @Override
  public void handle(final DeadLetter deadLetter) {
    final Consumer<DeadLettersListener> consumer = (actor) -> actor.handle(deadLetter);
    mailbox.send(new LocalMessage<DeadLettersListener>(actor, typedActor, consumer, "handle(DeadLetter)"));
  }
}
