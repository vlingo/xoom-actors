// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class LocalMessage<T> implements Message {
  protected final Actor actor;
  protected final Consumer<T> consumer;
  protected final Class<T> protocol;
  protected final String representation;
  
  public LocalMessage(final Actor actor, final Class<T> protocol, final Consumer<T> consumer, final String representation) {
    this.actor = actor;
    this.consumer = consumer;
    this.protocol = protocol;
    this.representation = representation;
  }

  public LocalMessage(final LocalMessage<T> message) {
    this.actor = message.actor;
    this.consumer = message.consumer;
    this.protocol = message.protocol;
    this.representation = message.representation;
  }

  @Override
  public Actor actor() {
    return actor;
  }

  @Override
  public void deliver() {
    if (actor.__internal__IsResumed()) {
      if (isStowed()) {
        internalDeliver(this);
      } else {
        internalDeliver(actor.__internal__Environment().suspended.swapWith(this));
      }
      actor.__internal__NextResuming();
    } else if (actor.isDispersing()) {
      internalDeliver(actor.__internal__Environment().stowage.swapWith(this));
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
    final DeadLetters deadLetters = actor.__internal__Environment().stage.world().deadLetters();
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
    } else if (actor.__internal__IsSuspend()) {
      actor.__internal__Environment().suspended.stow(message);
    } else if (actor.isStowing()) {
      actor.__internal__Environment().stowage.stow(message);
    } else {
      try {
        consumer.accept((T) actor);
      } catch (Throwable t) {
        actor.logger().log("Message#deliver(): Exception: " + t.getMessage() + " for Actor: " + actor + " sending: " + representation, t);
        actor.stage().handleFailureOf(new StageSupervisedActor(protocol, actor, t));
      }
    }
  }
}
