// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

import io.vlingo.actors.testkit.TestEnvironment;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestStateView;

public abstract class Actor implements Startable, Stoppable, TestStateView {
  private final Environment environment;

  public Address address() {
    return environment.address;
  }

  public DeadLetters deadLetters() {
    return environment.stage.world().deadLetters();
  }

  @Override
  public void start() {
  }

  @Override
  public boolean isStopped() {
    return environment.isStopped();
  }

  @Override
  public void stop() {
    if (!isStopped()) {
      if (environment.address.id() != World.DEADLETTERS_ID) {
        environment.stage.stop(this);
      }
    }
  }

  public TestState viewTestState() {
    // override for concrete actor state
    return new TestState();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != this.getClass()) {
      return false;
    }
    
    return environment.address.equals(((Actor) other).environment.address);
  }

  @Override
  public int hashCode() {
    return environment.address.hashCode();
  }

  @Override
  public String toString() {
    return "Actor[type=" + this.getClass().getSimpleName() + " address=" + this.environment.address + "]";
  }

  protected <T> T childActorFor(final Definition definition, final Class<T> protocol) {
    if (definition.supervisor() != null) {
      return environment.stage.actorFor(definition, protocol, this, definition.supervisor(), logger());
    } else {
      if (this instanceof Supervisor) {
        return environment.stage.actorFor(definition, protocol, this, environment.lookUpProxy(Supervisor.class), logger());
      } else {
        return environment.stage.actorFor(definition, protocol, this, null, logger());
      }
    }
  }

  protected Definition definition() {
    if (environment.isSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its definition.");
    }
    return environment.definition;
  }

  protected Logger logger() {
    return environment.logger;
  }

  protected Actor parent() {
    if (environment.isSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its parent.");
    }
    return environment.parent;
  }

  protected void secure() {
    environment.setSecured();
  }

  protected <T> T selfAs(final Class<T> protocol) {
    return environment.stage.actorProxyFor(protocol, this, environment.mailbox);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected OutcomeInterest selfAsOutcomeInterest(final Object reference) {
    final OutcomeAware outcomeAware = environment.stage.actorProxyFor(OutcomeAware.class, this, environment.mailbox);
    return new OutcomeInterestActorProxy(outcomeAware, reference);
  }

  protected final Stage stage() {
    if (environment.isSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its stage.");
    }
    return environment.stage;
  }

  protected Stage stageNamed(final String name) {
    return environment.stage.world().stageNamed(name);
  }

  //=======================================
  // stowing/dispersing
  //=======================================

  protected boolean isDispersing() {
    return environment.stowage.isDispersing();
  }

  protected void disperseStowedMessages() {
    environment.stowage.dispersingMode();
    __internal_SendFirst(environment.stowage);
  }

  protected boolean isStowing() {
    return environment.stowage.isStowing();
  }

  protected void stowMessages() {
    environment.stowage.stowingMode();
  }

  //=======================================
  // life cycle overrides
  //=======================================

  protected void beforeStart() {
    // override
  }

  protected void afterStop() {
    // override
  }

  protected void beforeRestart(final Throwable reason) {
    // override
    __internal__AfterStop();
  }

  protected void afterRestart(final Throwable reason) {
    // override
    __internal__BeforeStart();
  }

  //=======================================
  // internal implementation
  //=======================================

  protected Actor() {
    final Environment maybeEnvironment = ActorFactory.threadLocalEnvironment.get();
    this.environment = maybeEnvironment != null ? maybeEnvironment : new TestEnvironment();
    ActorFactory.threadLocalEnvironment.set(null);
    __internal__SendStart();
  }

  protected Environment __internal__Environment() {
    return environment;
  }

  protected void __internal__Stop() {
    environment.stop();
    
    __internal__AfterStop();
  }

  private void __internal__AfterStop() {
    try {
      afterStop();
    } catch (Throwable t) {
      logger().log("vlingo/actors: Actor afterStop() failed: " + t.getMessage(), t);
      environment.stage.handleFailureOf(new StageSupervisedActor(Stoppable.class, this, t));
    }
  }

  protected void __internal__BeforeStart() {
    try {
      beforeStart();
    } catch (Throwable t) {
      logger().log("vlingo/actors: Actor beforeStart() failed: " + t.getMessage());
      environment.stage.handleFailureOf(new StageSupervisedActor(Startable.class, this, t));
    }
  }

  protected void __internal__AfterRestart(Throwable throwable, Class<?> protocol) {
    try {
      afterRestart(throwable);
    } catch (Throwable t) {
      logger().log("vlingo/actors: Actor beforeStart() failed: " + t.getMessage());
      environment.stage.handleFailureOf(new StageSupervisedActor(Startable.class, this, t));
    }
  }

  protected void __internal__BeforeRestart(final Throwable reason, final Class<?> protocol) {
    try {
      beforeRestart(reason);
    } catch (Throwable t) {
      logger().log("vlingo/actors: Actor beforeRestart() failed: " + t.getMessage());
      environment.stage.handleFailureOf(new StageSupervisedActor(protocol, this, t));
    }
  }

  private void __internal__SendStart() {
    try {
      final Consumer<Startable> consumer = (actor) -> actor.start();
      final Message message = new LocalMessage<Startable>(this, Startable.class, consumer, "start()");
      environment.mailbox.send(message);
    } catch (Throwable t) {
      logger().log("vlingo/actors: Actor start() failed: " + t.getMessage());
      environment.stage.handleFailureOf(new StageSupervisedActor(Startable.class, this, t));
    }
  }

  private void __internal_SendFirst(final Stowage stowage) {
    final Message maybeMessage = stowage.head();
    if (maybeMessage != null) {
      //stowage.dump(logger());
      environment.mailbox.send(maybeMessage);
    }
  }

  //=======================================
  // supervisor/suspending/resuming
  //=======================================

  protected boolean __internal__IsResumed() {
    return environment.suspended.isDispersing();
  }

  protected void __internal__NextResuming() {
    if (__internal__IsResumed()) {
      __internal_SendFirst(environment.suspended);
    }
  }

  protected void __internal__Resume() {
    environment.suspended.dispersingMode();
    __internal_SendFirst(environment.suspended);
  }

  protected boolean __internal__IsSuspend() {
    return environment.suspended.isStowing();
  }

  protected void __internal__Suspend() {
    environment.suspended.stowingMode();
  }

  protected Supervisor __internal_Supervisor(final Class<?> protocol) {
    Supervisor supervisor = environment.maybeSupervisor;
    
    if (supervisor == null) {
      supervisor = environment.stage.commonSupervisorOr(protocol, environment.stage.world().defaultSupervisor());
    }
    
    return supervisor;
  }
}
