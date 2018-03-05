// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class Scheduled__Proxy implements Scheduled {
  private static final String representationIntervalSignal1 = "intervalSignal(Scheduled, Object)";

  private final Actor actor;
  private final Mailbox mailbox;

  public Scheduled__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public void intervalSignal(final Scheduled scheduled, final Object data) {
    if (!actor.isStopped()) {
      final Consumer<Scheduled> consumer = (actor) -> actor.intervalSignal(scheduled, data);
      mailbox.send(new LocalMessage<Scheduled>(actor, Scheduled.class, consumer, representationIntervalSignal1));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, representationIntervalSignal1));
    }
  }
}
