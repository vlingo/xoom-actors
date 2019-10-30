// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class LocalMessage<T> implements Message {
  Actor actor;
  Returns<Object> returns;
  Consumer<T> consumer;
  Class<T> protocol;
  String representation;

  @SuppressWarnings("unchecked")
  public LocalMessage(final Actor actor, final Class<T> protocol, final Consumer<T> consumer, final Returns<?> returns, final String representation) {
    this.actor = actor;
    this.consumer = consumer;
    this.protocol = protocol;
    this.representation = representation;
    this.returns = (Returns<Object>) returns;
  }

  public LocalMessage(final Actor actor, final Class<T> protocol, final Consumer<T> consumer, final String representation) {
    this(actor, protocol, consumer, null, representation);
  }

  public LocalMessage(final LocalMessage<T> message) {
    this(message.actor, message.protocol, message.consumer, message.returns, message.representation);
  }

  public LocalMessage(final Mailbox mailbox) {
    assert mailbox.isPreallocated();
  }

  @Override
  public Actor actor() {
    return actor;
  }

  @Override
  public void deliver() {
    internalDeliver(this);

//    if (actor.lifeCycle.isResuming()) {
//      if (isStowed()) {
//        internalDeliver(this);
//      } else {
//        internalDeliver(actor.lifeCycle.environment.suspended.swapWith(this));
//      }
//      actor.lifeCycle.nextResuming();
//    } else if (actor.isDispersing()) {
//      internalDeliver(this);
//      actor.lifeCycle.nextDispersing();
//    } else {
//      internalDeliver(this);
//    }
  }

  @Override
  public Class<?> protocol() {
    return protocol;
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
  @SuppressWarnings("unchecked")
  public void set(final Actor actor, final Class<?> protocol, final Consumer<?> consumer, final Returns<?> returns, final String representation) {
    this.actor = actor;
    this.consumer = (Consumer<T>) consumer;
    this.protocol = (Class<T>) protocol;
    this.representation = representation;
    this.returns = (Returns<Object>) returns;
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
      actor.logger().warn("vlingo/actors: MISSING DEAD LETTERS FOR: " + deadLetter);
    }
  }

  @SuppressWarnings("unchecked")
  private void internalDeliver(final Message message) {
    if (actor.isStopped()) {
      deadLetter();
//    } else if (actor.lifeCycle.isSuspended()) {
//      actor.lifeCycle.environment.suspended.stow(message);
//    } else if (actor.isStowing() && !actor.lifeCycle.environment.isStowageOverride(protocol)) {
//      actor.lifeCycle.environment.stowage.stow(message);
    } else {
      try {
        actor.returns.reset(returns);
        consumer.accept((T) actor);
        if (actor.returns.__internal__outcomeSet) {
          // USE THE FOLLOWING. this forces the same ce actor to be used for
          // all completes outcomes such that completes outcomes cannot be
          // delivered to the client out of order from the original ordered causes.
          actor.lifeCycle.environment.completesEventually(actor.returns).with(actor.returns.__internal__outcome);
          // DON'T USE THE FOLLOWING. it selects ce actors in round-robin order which
          // can easily cause clients to see outcomes of messages delivered later to
          // an actor before outcomes of messages delivered earlier to the same actor.
          //actor.lifeCycle.environment.stage.world().completesFor(completes).with(actor.completes.__internal__outcome);
        }
      } catch (Throwable t) {
        // Logging here duplicates logging provided by supervisor.
        // actor.logger().error("Message#deliver(): Exception: " + t.getMessage() + " for Actor: " + actor + " sending: " + representation, t);
        actor.stage().handleFailureOf(new StageSupervisedActor(protocol, actor, t));
      }
    }
  }
}
