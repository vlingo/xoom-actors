// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class LocalMessage<T> implements Message {
  final Actor actor;
  final Completes<Object> completes;
  final Consumer<T> consumer;
  final Class<T> protocol;
  final String representation;

  @SuppressWarnings("unchecked")
  public LocalMessage(final Actor actor, final Class<T> protocol, final Consumer<T> consumer, final Completes<?> completes, final String representation) {
    this.actor = actor;
    this.consumer = consumer;
    this.protocol = protocol;
    this.representation = representation;
    this.completes = (Completes<Object>) completes;
  }

  public LocalMessage(final Actor actor, final Class<T> protocol, final Consumer<T> consumer, final String representation) {
    this(actor, protocol, consumer, null, representation);
  }

  public LocalMessage(final LocalMessage<T> message) {
    this(message.actor, message.protocol, message.consumer, message.completes, message.representation);
  }

  @Override
  public Actor actor() {
    return actor;
  }

  @Override
  public void deliver() {
    if (actor.lifeCycle.isResuming()) {
      if (isStowed()) {
        internalDeliver(this);
      } else {
        internalDeliver(actor.lifeCycle.environment.suspended.swapWith(this));
      }
      actor.lifeCycle.nextResuming();
    } else if (actor.isDispersing()) {
      internalDeliver(this);
      actor.lifeCycle.nextDispersing();
    } else {
      internalDeliver(this);
    }
  }

  @Override
  public String representation() {
    return representation;
  }

  @Override
  public boolean isStowed() {
    return false;
  }

  @Override
  public String toString() {
    return "LocalMessage[" + representation() + "]";
  }

  private void deadLetter() {
    final DeadLetter deadLetter  = new DeadLetter(actor, representation);
    final DeadLetters deadLetters = actor.deadLetters();
    if (deadLetters != null) {
      deadLetters.failedDelivery(deadLetter);
    } else {
      actor.logger().log("vlingo/actors: MISSING DEAD LETTERS FOR: " + deadLetter);
    }
  }

  @SuppressWarnings("unchecked")
  private void internalDeliver(final Message message) {
    if (actor.isStopped()) {
      deadLetter();
    } else if (actor.lifeCycle.isSuspended()) {
      actor.lifeCycle.environment.suspended.stow(message);
    } else if (actor.isStowing() && !actor.lifeCycle.environment.isStowageOverride(protocol)) {
      actor.lifeCycle.environment.stowage.stow(message);
    } else {
      try {
        actor.completes = completes;
        consumer.accept((T) actor);
        if (actor.completes != null && actor.completes.hasOutcome()) {
          final Object outcome = actor.completes.outcome();
          actor.lifeCycle.environment.stage.world().completesFor(completes).with(outcome);
        }
      } catch (Throwable t) {
        actor.logger().log("Message#deliver(): Exception: " + t.getMessage() + " for Actor: " + actor + " sending: " + representation, t);
        actor.stage().handleFailureOf(new StageSupervisedActor(protocol, actor, t));
      }
    }
  }
}
