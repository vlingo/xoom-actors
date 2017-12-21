// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Message {
  public final Actor actor;
  public final Method method;
  public final Object[] args;

  protected Message(final Actor actor, final Method method, final Object[] args) {
    this.actor = actor;
    this.method = method;
    this.args = args;
  }

  public void deliver() {
    if (actor.isStopped()) {
      final DeadLetter deadLetter  = new DeadLetter(actor, method, args);
      final DeadLetters deadLetters = actor.__internalOnlyStage().world().deadLetters();
      if (deadLetters != null) {
        deadLetters.failedDelivery(deadLetter);
      } else {
        // TODO: Log
        System.out.println("vlingo/actors: MISSING DEAD LETTERS FOR: " + deadLetter);
      }
      return;
    }
    
    try {
      method.invoke(actor, args);
    } catch (IllegalAccessException e) {
      // TODO: handle
      System.out.println("Message#deliver(): IllegalAccessException: " + e.getMessage() + " for Actor: " + actor);
    } catch (IllegalArgumentException e) {
      // TODO: handle
      System.out.println("Message#deliver(): IllegalArgumentException: " + e.getMessage() + " for Actor: " + actor);
    } catch (InvocationTargetException e) {
      // TODO: handle
      System.out.println("Message#deliver(): InvocationTargetException: " + e.getMessage() + " for Actor: " + actor);
    } catch (Exception e) {
      // TODO: handle
      System.out.println("Message#deliver(): Exception: " + e.getMessage() + " for Actor: " + actor);
    }
  }
}
