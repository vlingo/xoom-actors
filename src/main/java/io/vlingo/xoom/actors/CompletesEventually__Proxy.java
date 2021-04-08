// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import io.vlingo.xoom.common.SerializableConsumer;

public class CompletesEventually__Proxy implements CompletesEventually {
  private static final String representationConclude0 = "conclude()";
  private static final String representationStop1 = "stop()";
  private static final String representationWith2 = "with(Object)";

  private final Actor actor;
  private final Mailbox mailbox;

  public CompletesEventually__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public Address address() {
    return actor.address();
  }

  @Override
  public void conclude() {
    if (!actor.isStopped()) {
      final SerializableConsumer<Stoppable> consumer = (actor) -> actor.conclude();
      if (mailbox.isPreallocated()) { mailbox.send(actor, Stoppable.class, consumer, null, representationConclude0); }
      else { mailbox.send(new LocalMessage<Stoppable>(actor, Stoppable.class, consumer, representationConclude0)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, representationConclude0));
    }
  }

  @Override
  public boolean isStopped() {
    return actor.isStopped();
  }

  @Override
  public void stop() {
    if (!actor.isStopped()) {
      final SerializableConsumer<Stoppable> consumer = (actor) -> actor.stop();
      if (mailbox.isPreallocated()) { mailbox.send(actor, Stoppable.class, consumer, null, representationStop1); }
      else { mailbox.send(new LocalMessage<Stoppable>(actor, Stoppable.class, consumer, representationStop1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, representationStop1));
    }
  }

  @Override
  public void with(final Object outcome) {
    if (!actor.isStopped()) {
      final SerializableConsumer<CompletesEventually> consumer = (actor) -> actor.with(outcome);
      if (mailbox.isPreallocated()) { mailbox.send(actor, CompletesEventually.class, consumer, null, representationWith2); }
      else { mailbox.send(new LocalMessage<CompletesEventually>(actor, CompletesEventually.class, consumer, representationWith2)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, representationWith2));
    }
  }
}
