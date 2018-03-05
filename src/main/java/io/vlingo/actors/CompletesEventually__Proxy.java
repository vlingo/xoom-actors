// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class CompletesEventually__Proxy implements CompletesEventually {
  private static final String representationStop1 = "stop()";
  private static final String representationWith2 = "with(Object)";

  private final Actor actor;
  private final Mailbox mailbox;

  public CompletesEventually__Proxy(final Actor actor, final Mailbox mailbox) {
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
      final Consumer<Stoppable> consumer = (actor) -> actor.stop();
      mailbox.send(new LocalMessage<Stoppable>(actor, Stoppable.class, consumer, representationStop1));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, representationStop1));
    }
  }

  @Override
  public void with(final Object outcome) {
    if (!actor.isStopped()) {
      final Consumer<CompletesEventually> consumer = (actor) -> actor.with(outcome);
      mailbox.send(new LocalMessage<CompletesEventually>(actor, CompletesEventually.class, consumer, representationWith2));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, representationWith2));
    }
  }
}
