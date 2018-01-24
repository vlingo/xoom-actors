// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

final class LifeCycle {
  final Environment environment;

  LifeCycle(final Actor actor, final Environment environment) {
    this.environment = environment;
  }

  @Override
  public int hashCode() {
    return address().hashCode();
  }

  Address address() {
    return environment.address;
  }

  Definition definition() {
    if (environment.isSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its definition.");
    }
    return environment.definition;
  }

  <T> T lookUpProxy(final Class<T> protocol) {
    return environment.lookUpProxy(protocol);
  }

  boolean isSecured() {
     return environment.isSecured();
  }

  void secure() {
    environment.setSecured();
  }

  boolean isStopped() {
    return environment.isStopped();
  }

  void stop(final Actor actor) {
    environment.stop();
    
    afterStop(actor);
  }

  //=======================================
  // standard life cycle
  //=======================================

  void afterStop(final Actor actor) {
    try {
      actor.afterStop();
    } catch (Throwable t) {
      environment.logger.log("vlingo/actors: Actor afterStop() failed: " + t.getMessage(), t);
      environment.stage.handleFailureOf(new StageSupervisedActor(Stoppable.class, actor, t));
    }
  }

  void beforeStart(final Actor actor) {
    try {
      actor.beforeStart();
    } catch (Throwable t) {
      environment.logger.log("vlingo/actors: Actor beforeStart() failed: " + t.getMessage());
      environment.stage.handleFailureOf(new StageSupervisedActor(Startable.class, actor, t));
    }
  }

  void afterRestart(final Actor actor, final Throwable throwable, final Class<?> protocol) {
    try {
      actor.afterRestart(throwable);
    } catch (Throwable t) {
      environment.logger.log("vlingo/actors: Actor beforeStart() failed: " + t.getMessage());
      environment.stage.handleFailureOf(new StageSupervisedActor(Startable.class, actor, t));
    }
  }

  void beforeRestart(final Actor actor, final Throwable reason, final Class<?> protocol) {
    try {
      actor.beforeRestart(reason);
    } catch (Throwable t) {
      environment.logger.log("vlingo/actors: Actor beforeRestart() failed: " + t.getMessage());
      environment.stage.handleFailureOf(new StageSupervisedActor(protocol, actor, t));
    }
  }

  void sendStart(final Actor targetActor) {
    try {
      final Consumer<Startable> consumer = (actor) -> actor.start();
      final Message message = new LocalMessage<Startable>(targetActor, Startable.class, consumer, "start()");
      environment.mailbox.send(message);
    } catch (Throwable t) {
      environment.logger.log("vlingo/actors: Actor start() failed: " + t.getMessage());
      environment.stage.handleFailureOf(new StageSupervisedActor(Startable.class, targetActor, t));
    }
  }

  //=======================================
  // stowing/dispersing
  //=======================================

  boolean isDispersing() {
    return environment.stowage.isDispersing();
  }

  void disperseStowedMessages() {
    environment.stowage.dispersingMode();
    sendFirstIn(environment.stowage);
  }

  void sendFirstIn(final Stowage stowage) {
    final Message maybeMessage = stowage.head();
    if (maybeMessage != null) {
      //stowage.dump(environment.logger);
      environment.mailbox.send(maybeMessage);
    }
  }

  boolean isStowing() {
    return environment.stowage.isStowing();
  }

  void stowMessages() {
    environment.stowage.stowingMode();  
  }

  //=======================================
  // supervisor/suspending/resuming
  //=======================================

  boolean isResuming() {
    return environment.suspended.isDispersing();
  }

  void nextResuming() {
    if (isResuming()) {
      sendFirstIn(environment.suspended);
    }
  }

  void resume() {
    environment.suspended.dispersingMode();
    sendFirstIn(environment.suspended);
  }

  boolean isSuspended() {
    return environment.suspended.isStowing();
  }

  void suspend() {
    environment.suspended.stowingMode();
  }

  Supervisor supervisor(final Class<?> protocol) {
    Supervisor supervisor = environment.maybeSupervisor;
    
    if (supervisor == null) {
      supervisor = environment.stage.commonSupervisorOr(protocol, environment.stage.world().defaultSupervisor());
    }
    
    return supervisor;
  }
}
