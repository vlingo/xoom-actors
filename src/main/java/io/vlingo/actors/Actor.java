// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.testkit.TestEnvironment;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestStateView;
import io.vlingo.common.Completes;
import io.vlingo.common.ResultCompletes;
import io.vlingo.common.Scheduler;

/**
 * The abstract base class of all concrete instance. This base provides common
 * facilities and life cycle processing for all Actors.
 */
public abstract class Actor implements Startable, Stoppable, TestStateView {
  final ResultCompletes completes;
  final LifeCycle lifeCycle;

  /**
   * Answers the address of this Actor.
   * @return Address
   */
  public Address address() {
    return lifeCycle.address();
  }

  /**
   * Answers the DeadLetters for this Actor.
   * @return DeadLetters
   */
  public DeadLetters deadLetters() {
    return lifeCycle.environment.stage.world().deadLetters();
  }

  /**
   * Answers the Scheduler for this Actor.
   * @return Scheduler
   */
  public Scheduler scheduler() {
    return lifeCycle.environment.stage.scheduler();
  }

  /**
   * The default implementation of start(), which is a no-op. Override if needed.
   */
  @Override
  public void start() {
  }

  /**
   * Answers whether or not this Actor has been stopped or is in the process or stopping.
   * @return boolean
   */
  @Override
  public boolean isStopped() {
    return lifeCycle.isStopped();
  }

  /**
   * Initiates the process or stopping this Actor all all of its children.
   */
  @Override
  public void stop() {
    if (!isStopped()) {
      if (lifeCycle.address().id() != World.DEADLETTERS_ID) {
        // TODO: remove this actor as a child on parent
        lifeCycle.stop(this);
      }
    }
  }

  /**
   * Answers the TestState for this Actor. Override to provide a snapshot of the current Actor state.
   * @return TestState
   */
  public TestState viewTestState() {
    // override for concrete actor state
    return new TestState();
  }

  /**
   * Answers whether or not this Actor is equal to other.
   * @param other the Object to which this Actor is compared
   * @return boolean
   */
  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != this.getClass()) {
      return false;
    }
    
    return address().equals(((Actor) other).lifeCycle.address());
  }

  /**
   * Answers the int hash code of this Actor.
   * @return int
   */
  @Override
  public int hashCode() {
    return lifeCycle.hashCode();
  }

  /**
   * Answers the String representation of this Actor.
   * @return String
   */
  @Override
  public String toString() {
    return "Actor[type=" + this.getClass().getSimpleName() + " address=" + address() + "]";
  }

  /**
   * Answers the parent Actor of this Actor. (INTERNAL ONLY)
   * @return Actor
   */
  Actor parent() {
    if (lifeCycle.environment.isSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its parent.");
    }
    return lifeCycle.environment.parent;
  }

  /**
   * Initializes the newly created Actor.
   */
  protected Actor() {
    final Environment maybeEnvironment = ActorFactory.threadLocalEnvironment.get();
    this.lifeCycle = new LifeCycle(maybeEnvironment != null ? maybeEnvironment : new TestEnvironment());
    ActorFactory.threadLocalEnvironment.set(null);
    this.completes = new ResultCompletes();
  }

  /**
   * Answers the T protocol for the child Actor to be created by this parent Actor.
   * @param definition the Definition of the child Actor to be created by this parent Actor
   * @param protocol the Class&lt;T&gt; protocol of the child Actor
   * @param <T> the protocol type
   * @return T
   */
  protected <T> T childActorFor(final Definition definition, final Class<T> protocol) {
    if (definition.supervisor() != null) {
      return lifeCycle.environment.stage.actorFor(definition, protocol, this, definition.supervisor(), logger());
    } else {
      if (this instanceof Supervisor) {
        return lifeCycle.environment.stage.actorFor(definition, protocol, this, lifeCycle.lookUpProxy(Supervisor.class), logger());
      } else {
        return lifeCycle.environment.stage.actorFor(definition, protocol, this, null, logger());
      }
    }
  }

  /**
   * Answers the Completes&lt;T&gt; instance for this Actor, or null if the behavior of the currently
   * delivered Message does not answer a Completes&lt;T&gt;.
   * @param <T> the protocol type
   * @return Completes&lt;T&gt;
   */
  @SuppressWarnings("unchecked")
  protected <T> Completes<T> completes() {
    if (completes == null) {
      throw new IllegalStateException("Completes is not available for this protocol behavior.");
    }
    return (Completes<T>) completes;
  }

  /**
   * Answers a CompletesEventually if the behavior of the currently
   * delivered Message does answers a Completes&lt;T&gt;. Otherwise the outcome
   * is unpredictable.
   * @return CompletesEventually
   */
  protected CompletesEventually completesEventually() {
    return lifeCycle.environment.stage.world().completesFor(completes.clientCompletes());
  }

  /**
   * Answers the Definition of this Actor.
   * @return Definition
   */
  protected Definition definition() {
    return lifeCycle.definition();
  }

  /**
   * Answers the Logger of this Actor.
   * @return Logger
   */
  protected Logger logger() {
    return lifeCycle.environment.logger;
  }

  /**
   * Answers the parent of this Actor as the T protocol.
   * @param protocol the Class&lt;T&gt; of the protocol
   * @param <T> the protocol type
   * @return T
   */
  protected <T> T parentAs(final Class<T> protocol) {
    if (lifeCycle.environment.isSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its parent.");
    }
    final Actor parent = lifeCycle.environment.parent;
    return lifeCycle.environment.stage.actorProxyFor(protocol, parent, parent.lifeCycle.environment.mailbox);
  }

  /**
   * Secures this Actor. (INTERNAL ONLY)
   */
  protected void secure() {
    lifeCycle.secure();
  }

  /**
   * Answers this Actor as a T protocol. This Actor must implement the Class&lt;T&gt; protocol.
   * @param protocol the Class&lt;T&gt; protocol
   * @param <T> the protocol type
   * @return T
   */
  protected <T> T selfAs(final Class<T> protocol) {
    return lifeCycle.environment.stage.actorProxyFor(protocol, this, lifeCycle.environment.mailbox);
  }

  /**
   * Answers the Stage of this Actor.
   * @return Stage
   */
  protected final Stage stage() {
    if (lifeCycle.environment.isSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its stage.");
    }
    return lifeCycle.environment.stage;
  }

  /**
   * Answers the Stage of the given name.
   * @param name the String name of the Stage to find
   * @return Stage
   */
  protected Stage stageNamed(final String name) {
    return lifeCycle.environment.stage.world().stageNamed(name);
  }

  //=======================================
  // stowing/dispersing
  //=======================================

  /**
   * Answers whether this Actor is currently dispersing previously stowed messages.
   * @return boolean
   */
  protected boolean isDispersing() {
    return lifeCycle.isDispersing();
  }

  /**
   * Starts the process of dispersing any messages stowed for this Actor.
   */
  protected void disperseStowedMessages() {
    lifeCycle.disperseStowedMessages();
  }

  /**
   * Answers whether this Actor is currently stowing messages.
   * @return boolean
   */
  protected boolean isStowing() {
    return lifeCycle.isStowing();
  }

  /**
   * Starts the process of stowing messages for this Actor, and registers stowageOverrides as
   * the protocol that will trigger dispersal.
   * @param stowageOverrides the Class&lt;T&gt; protocol that will trigger dispersal
   */
  protected void stowMessages(final Class<?>... stowageOverrides) {
    lifeCycle.stowMessages();
    lifeCycle.environment.stowageOverrides(stowageOverrides);
  }

  //=======================================
  // life cycle overrides
  //=======================================

  /**
   * The message delivered before the Actor has fully started. Override to implement.
   */
  protected void beforeStart() {
    // override
  }

  /**
   * The message delivered after the Actor has fully stopped. Override to implement.
   */
  protected void afterStop() {
    // override
  }

  /**
   * The message delivered before the Actor has been restarted by its supervisor due to an exception.
   * Override to implement.
   * @param reason the Throwable cause of the supervision restart
   */
  protected void beforeRestart(final Throwable reason) {
    // override
    lifeCycle.afterStop(this);
  }

  /**
   * The message delivered after the Actor has been restarted by its supervisor due to an exception.
   * Override to implement.
   * @param reason the Throwable cause of the supervision restart
   */
  protected void afterRestart(final Throwable reason) {
    // override
    lifeCycle.beforeStart(this);
  }

  /**
   * The message delivered before the Actor has been resumed by its supervisor due to an exception.
   * Override to implement.
   * @param reason the Throwable cause of the supervision resume
   */
  protected void beforeResume(final Throwable reason) {
    // override
  }
}
