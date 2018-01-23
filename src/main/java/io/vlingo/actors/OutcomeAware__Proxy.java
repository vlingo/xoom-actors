// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class OutcomeAware__Proxy<O, R> implements OutcomeAware<O, R> {
  private static final String failureOutcomeRepesentation1 = "failureOutcome(Outcome<O>, R)";
  private static final String successfulOutcomeRepesentation2 = "successfulOutcome(Outcome<O>, R)";
  
  private final Actor actor;
  private final Mailbox mailbox;

  public OutcomeAware__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void failureOutcome(final Outcome<O> outcome, R reference) {
    if (!actor.isStopped()) {
      final Consumer<OutcomeAware<O, R>> consumer = (actor) -> actor.failureOutcome(outcome, reference);
      mailbox.send(new LocalMessage(actor, OutcomeAware.class, consumer, failureOutcomeRepesentation1));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, failureOutcomeRepesentation1));
    }
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void successfulOutcome(final Outcome<O> outcome, R reference) {
    if (!actor.isStopped()) {
      final Consumer<OutcomeAware<O, R>> consumer = (actor) -> actor.successfulOutcome(outcome, reference);
      mailbox.send(new LocalMessage(actor, OutcomeAware.class, consumer, successfulOutcomeRepesentation2));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, successfulOutcomeRepesentation2));
    }
  }
}
