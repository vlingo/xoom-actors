// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.testkit.TestContext;
import io.vlingo.actors.testkit.TestEnvironment;
import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestStateView;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduler;

/**
 * The abstract base class of all concrete {@code Actor} types. This base provides common
 * facilities and life cycle processing for all {@code Actor} types.
 */
public abstract class Actor implements Startable, Stoppable, TestStateView {
  private final static String Paused = "#paused";

  final ResultCompletes completes;
  final LifeCycle lifeCycle;

  /**
   * Answers the {@code address} of this {@code Actor}.
   * @return Address
   */
  public Address address() {
    return lifeCycle.address();
  }

  /**
   * Answers the {@code DeadLetters} for this {@code Actor}.
   * @return DeadLetters
   */
  public DeadLetters deadLetters() {
    return lifeCycle.environment.stage.world().deadLetters();
  }

  /**
   * Answers the {@code Scheduler} for this {@code Actor}.
   * @return Scheduler
   */
  public Scheduler scheduler() {
    return lifeCycle.environment.stage.scheduler();
  }

  /**
   * The default implementation of {@code start()}, which is a no-op. Override if needed.
   */
  @Override
  public void start() {
  }

  /**
   * Answers whether or not this {@code Actor} has been stopped or is in the process or stopping.
   * @return boolean
   */
  @Override
  public boolean isStopped() {
    return lifeCycle.isStopped();
  }

  /**
   * Initiates the process or stopping this {@code Actor} and all of its children.
   */
  @Override
  public void stop() {
    if (!isStopped()) {
      if (lifeCycle.address().id() != World.DEADLETTERS_ID) {
        // TODO: remove this actor as a child on parent
        lifeCycle.suspend();
        lifeCycle.stop(this);
      }
    }
  }

  /**
   * Received from the surrounding {@code testkit.TestActor} to indicate
   * that it is in use, enabling any special test initialization as
   * needed. This is received (1) with a {@code TestContext} when the
   * {@code TestActor} is first constructed, and (2) with {@code null}
   * before each {@code TestMailbox} delivery.
   * @param context the TestContext
   */
  public void viewTestStateInitialization(final TestContext context) {
    // override for concrete actor test signaling
  }

  /**
   * Answers the {@code TestState} for this {@code Actor}. Override to provide a snapshot of the current {@code Actor} state.
   * @return TestState
   */
  public TestState viewTestState() {
    // override for concrete actor state
    return new TestState();
  }

  /**
   * Answers whether or not this {@code Actor} is equal to {@code other}.
   * @param other the {@code Object} to which this {@code Actor} is compared
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
   * Answers the {@code int} hash code of this {@code Actor}.
   * @return int
   */
  @Override
  public int hashCode() {
    return lifeCycle.hashCode();
  }

  /**
   * Answers the {@code String} representation of this {@code Actor}.
   * @return String
   */
  @Override
  public String toString() {
    return "Actor[type=" + this.getClass().getSimpleName() + " address=" + address() + "]";
  }

  /**
   * Answers the parent {@code Actor} of this {@code Actor}. (INTERNAL ONLY)
   * @return Actor
   */
  Actor parent() {
    if (lifeCycle.environment.isSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its parent.");
    }
    return lifeCycle.environment.parent;
  }

  /**
   * Initializes the newly created {@code Actor}.
   */
  protected Actor() {
    final Environment maybeEnvironment = ActorFactory.threadLocalEnvironment.get();
    this.lifeCycle = new LifeCycle(maybeEnvironment != null ? maybeEnvironment : new TestEnvironment());
    ActorFactory.threadLocalEnvironment.set(null);
    this.completes = new ResultCompletes();
  }

  /**
   * Answers the {@code T} protocol for the child {@code Actor} to be created by this parent {@code  Actor}.
   * @param <T> the protocol type
   * @param protocol the {@code Class<T>} protocol of the child {@code Actor}
   * @param definition the {@code Definition} of the child {@code Actor} to be created by this parent {@code Actor}
   * @return T
   */
  protected <T> T childActorFor(final Class<T> protocol, final Definition definition) {
    if (definition.supervisor() != null) {
      return lifeCycle.environment.stage.actorFor(protocol, definition, this, definition.supervisor(), logger());
    } else {
      if (this instanceof Supervisor) {
        return lifeCycle.environment.stage.actorFor(protocol, definition, this, lifeCycle.lookUpProxy(Supervisor.class), logger());
      } else {
        return lifeCycle.environment.stage.actorFor(protocol, definition, this, null, logger());
      }
    }
  }

  /**
   * Answers the {@code Completes<T>} instance for this {@code Actor}, or {@code null} if the behavior of the currently
   * delivered {@code Message} does not answer a {@code Completes<T>}.
   * @param <T> the protocol type
   * @return {@code Completes<T>}
   */
  @SuppressWarnings("unchecked")
  protected <T> Completes<T> completes() {
    if (completes == null) {
      throw new IllegalStateException("Completes is not available for this protocol behavior.");
    }
    return (Completes<T>) completes;
  }

  /**
   * Answers a {@code CompletesEventually} if the behavior of the currently
   * delivered {@code Message} does answers a {@code Completes<T>}. Otherwise the outcome
   * is undefined.
   * @return CompletesEventually
   */
  protected CompletesEventually completesEventually() {
    return lifeCycle.environment.completesEventually(completes);
  }

  /**
   * Answers the {@code Definition} of this {@code Actor}.
   * @return Definition
   */
  protected Definition definition() {
    return lifeCycle.definition();
  }

  /**
   * Answers the {@code Logger} of this {@code Actor}.
   * @return Logger
   */
  protected Logger logger() {
    return lifeCycle.environment.logger;
  }

  /**
   * Answers the parent of this {@code Actor} as the {@code T} protocol.
   * @param protocol the {@code Class<T>} of the protocol
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
   * Secures this {@code Actor}. (INTERNAL ONLY)
   */
  protected void secure() {
    lifeCycle.secure();
  }

  /**
   * Answers this {@code Actor} as a {@code T} protocol. This {@code Actor} must implement the {@code Class<T>} protocol.
   * @param protocol the {@code Class<T>} protocol
   * @param <T> the protocol type
   * @return T
   */
  protected <T> T selfAs(final Class<T> protocol) {
    return lifeCycle.environment.stage.actorProxyFor(protocol, this, lifeCycle.environment.mailbox);
  }

  /**
   * Answers the {@code Stage} of this {@code Actor}.
   * @return Stage
   */
  protected final Stage stage() {
    if (lifeCycle.environment.isSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its stage.");
    }
    return lifeCycle.environment.stage;
  }

  /**
   * Answers the {@code Stage} of the given name.
   * @param name the {@code String} name of the {@code Stage} to find
   * @return Stage
   */
  protected Stage stageNamed(final String name) {
    return lifeCycle.environment.stage.world().stageNamed(name);
  }

  //=======================================
  // stowing/dispersing
  //=======================================

  /**
   * Starts the process of dispersing any messages stowed for this {@code Actor}.
   */
  protected void disperseStowedMessages() {
    lifeCycle.environment.mailbox.resume(Paused);
  }

  /**
   * Starts the process of stowing messages for this {@code Actor}, and registers {@code stowageOverrides} as
   * the protocol that will trigger dispersal.
   * @param stowageOverrides the {@code Class<T>} protocol that will trigger dispersal
   */
  protected void stowMessages(final Class<?>... stowageOverrides) {
    lifeCycle.environment.mailbox.suspendExceptFor(Paused, stowageOverrides);
  }

  //=======================================
  // life cycle overrides
  //=======================================

  /**
   * The message delivered before the {@code Actor} has fully started. Override to implement.
   */
  protected void beforeStart() {
    // override
  }

  /**
   * The message delivered after the {@code Actor} has fully stopped. Override to implement.
   */
  protected void afterStop() {
    // override
  }

  /**
   * The message delivered before the {@code Actor} has been restarted by its supervisor due to an exception.
   * Override to implement.
   * @param reason the {@code Throwable} cause of the supervision restart
   */
  protected void beforeRestart(final Throwable reason) {
    // override
    lifeCycle.afterStop(this);
  }

  /**
   * The message delivered after the {@code Actor} has been restarted by its supervisor due to an exception.
   * Override to implement.
   * @param reason the {@code Throwable} cause of the supervision restart
   */
  protected void afterRestart(final Throwable reason) {
    // override
    lifeCycle.beforeStart(this);
  }

  /**
   * The message delivered before the {@code Actor} has been resumed by its supervisor due to an exception.
   * Override to implement.
   * @param reason the {@code Throwable} cause of the supervision resume
   */
  protected void beforeResume(final Throwable reason) {
    // override
  }
}
