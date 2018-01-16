// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class LocalMessage<T> implements Message {
  private final Actor actor;
  private final Consumer<T> consumer;
  private final String representation;
  private final T typedActor;
  
  public LocalMessage(final Actor actor, final T typedActor, final Consumer<T> consumer, final String representation) {
    this.actor = actor;
    this.consumer = consumer;
    this.typedActor = typedActor;
    this.representation = representation;
  }

  @Override
  public Actor actor() {
    return actor;
  }

  @Override
  public void deliver() {
    if (actor.isStopped()) {
      deadLetter();
      return;
    }
    
    try {
      consumer.accept(typedActor);
    } catch (Exception e) {
      // TODO: handle
      System.out.println("Message#deliver(): Exception: " + e.getMessage() + " for Actor: " + actor + " sending: " + representation);
    }
  }

  @Override
  public String representation() {
    return representation;
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
      // TODO: Log
      System.out.println("vlingo/actors: MISSING DEAD LETTERS FOR: " + deadLetter);
    }
  }
}
