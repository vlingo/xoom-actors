// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class OutcomeInterest__Proxy<O> implements OutcomeInterest<O> {
  private static final String failureOutcomeRepesentation1 = "failureOutcome(Outcome<O>)";
  private static final String successfulOutcomeRepesentation2 = "successfulOutcome(Outcome<O>)";
  
  private final Actor actor;
  private final Mailbox mailbox;

  public OutcomeInterest__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void failureOutcome(Outcome<O> outcome) {
    if (!actor.isStopped()) {
      final Consumer<OutcomeInterest<O>> consumer = (actor) -> actor.failureOutcome(outcome);
      mailbox.send(new LocalMessage(actor, Cancellable.class, consumer, failureOutcomeRepesentation1));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, failureOutcomeRepesentation1));
    }
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void successfulOutcome(final Outcome<O> outcome) {
    if (!actor.isStopped()) {
      final Consumer<OutcomeInterest<O>> consumer = (actor) -> actor.successfulOutcome(outcome);
      mailbox.send(new LocalMessage(actor, Cancellable.class, consumer, successfulOutcomeRepesentation2));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, successfulOutcomeRepesentation2));
    }
  }
}
