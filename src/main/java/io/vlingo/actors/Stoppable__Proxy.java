// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class Stoppable__Proxy implements Stoppable {
  private final Actor actor;
  private final Mailbox mailbox;

  public Stoppable__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public void conclude() {
    if (!actor.isStopped()) {
      final Consumer<Stoppable> consumer = (actor) -> actor.conclude();
      if (mailbox.isPreallocated()) { mailbox.send(actor, Stoppable.class, consumer, null, "conclude()"); }
      else { mailbox.send(new LocalMessage<Stoppable>(actor, Stoppable.class, consumer, "conclude()")); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, "conclude()"));
    }
  }

  @Override
  public boolean isStopped() {
    return actor.isStopped();
  }

  @Override
  public void stop() {
    if (!actor.isStopped()) {
      final Consumer<Stoppable> consumer = (actor) -> actor.stop();
      if (mailbox.isPreallocated()) { mailbox.send(actor, Stoppable.class, consumer, null, "stop()"); }
      else { mailbox.send(new LocalMessage<Stoppable>(actor, Stoppable.class, consumer, "stop()")); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, "stop()"));
    }
  }
}
