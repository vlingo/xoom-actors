// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import io.vlingo.xoom.actors.testkit.TestContext;
import io.vlingo.xoom.actors.testkit.TestEnvironment;
import io.vlingo.xoom.actors.testkit.TestState;
import io.vlingo.xoom.actors.testkit.TestStateView;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Scheduler;

/**
 * The abstract base class of all concrete {@code Actor} types. This base provides common
 * facilities and life cycle processing for all {@code Actor} types.
 */
public abstract class Actor implements Startable, Stoppable, Relocatable, TestStateView {
  final ResultReturns returns;
  final LifeCycle lifeCycle;

  /**
   * Answers the {@code address} of this {@code Actor}.
   * @return Address
   */
  public Address address() {
    return lifeCycle.address();
  }

  /**
   * @see io.vlingo.xoom.actors.Stoppable#conclude()
   */
  @Override
  public void conclude() {
    selfAs(Stoppable.class).stop();
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
   * @see io.vlingo.xoom.actors.Relocatable#stateSnapshot(java.lang.Object)
   */
  @Override
  public <S> void stateSnapshot(final S stateSnapshot) {
    // no-op
  }

  /**
   * @see io.vlingo.xoom.actors.Relocatable#stateSnapshot()
   */
  @Override
  public <S> S stateSnapshot() {
    return null; // no-op
  }

  /**
   * The default implementation of {@code start()}, which is a no-op. Override if needed.
   *
   * @see io.vlingo.xoom.actors.Startable#start()
   */
  @Override
  public void start() {
  }

  /**
   * Answers whether or not this {@code Actor} has been stopped or is in the process or stopping.
   * @return boolean
   *
   * @see io.vlingo.xoom.actors.Stoppable#isStopped()
   */
  @Override
  public boolean isStopped() {
    return lifeCycle.isStopped();
  }

  /**
   * Initiates the process of stopping this {@code Actor} and all of its children.
   *
   * @see io.vlingo.xoom.actors.Stoppable#stop()
   */
  @Override
  public void stop() {
    if (!isStopped()) {
      if (lifeCycle.address().id() != World.DEADLETTERS_ID) {
        // TODO: remove this actor as a child on parent
        lifeCycle.suspendForStop();
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
  @Override
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
    this.lifeCycle = new LifeCycle(maybeEnvironment != null ? maybeEnvironment : new TestEnvironment(), new Evictable(this));
    ActorFactory.threadLocalEnvironment.set(null);
    this.returns = new ResultReturns();
  }

  /**
   * Answer my internal {@code Completes<R>} from {@code completes()} after preparing
   * for the {@code eventualOutcome} to be set in my {@code completesEventually()}.
   * @param eventualOutcome the {@code Completes<R>} the provides an eventual outcome
   * @param <R> the type of eventual outcome
   * @return {@code Completes<R>}
   */
  protected <R> Completes<R> answerFrom(final Completes<R> eventualOutcome) {
    final CompletesEventually completes = completesEventually();
    eventualOutcome
            .andThenConsume((R value) -> completes.with(value))
            .otherwiseConsume((R value) -> completes.with(value));
    return completes();
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
   * Answers the {@code Protocols} for the child {@code Actor} to be created by this parent {@code  Actor}.
   * @param protocols the {@code Class<T>[]} protocols of the child {@code Actor}
   * @param definition the {@code Definition} of the child {@code Actor} to be created by this parent {@code Actor}
   * @return Protocols
   */
  protected Protocols childActorFor(final Class<?>[] protocols, final Definition definition) {
    if (definition.supervisor() != null) {
      return lifeCycle.environment.stage.actorFor(protocols, definition, this, definition.supervisor(), logger());
    } else {
      if (this instanceof Supervisor) {
        return lifeCycle.environment.stage.actorFor(protocols, definition, this, lifeCycle.lookUpProxy(Supervisor.class), logger());
      } else {
        return lifeCycle.environment.stage.actorFor(protocols, definition, this, null, logger());
      }
    }
  }

  /**
   * Answers the {@code Completes<T>} instance for this {@code Actor}, or throws {@code IllegalStateException} if the behavior of the currently
   * delivered {@code Message} does not answer a {@code Completes<T>}.
   * @param <T> the protocol type
   * @return {@code Completes<T>}
   */
  @SuppressWarnings("unchecked")
  protected <T> Completes<T> completes() {
    if (returns == null || returns.__internal__clientReturns == null || !returns.__internal__clientReturns.isCompletes()) {
      throw new IllegalStateException("Completes is not available for this protocol behavior; return type must be Completes<T>.");
    }
    return (Completes<T>) returns;
  }

  /**
   * Answers the {@code CompletableFuture<T>} instance for this {@code Actor}, or throws {@code IllegalStateException} if the behavior of the currently
   * delivered {@code Message} does not answer a {@code CompletableFuture<T>}.
   * @param <T> the protocol type
   * @return {@code CompletableFuture<T>}
   */
  @SuppressWarnings("unchecked")
  protected <T> CompletableFuture<T> completableFuture() {
    if (returns == null || returns.__internal__clientReturns == null || !returns.__internal__clientReturns.isCompletableFuture()) {
      throw new IllegalStateException("CompletableFuture is not available for this protocol behavior; return type must be CompletableFuture<T>.");
    }
    return (CompletableFuture<T>) returns.asCompletableFuture();
  }

  /**
   * Answers the {@code Future<T>} instance for this {@code Actor}, or throws {@code IllegalStateException} if the behavior of the currently
   * delivered {@code Message} does not answer a {@code Future<T>}.
   * @param <T> the protocol type
   * @param callable the {@code Callable<T>} producing the outcome for the {@code Future<T>} to answer
   * @return {@code Future<T>}
   */
  @SuppressWarnings("unchecked")
  protected <T> Future<T> future(final Callable<T> callable) {
    if (returns == null || returns.__internal__clientReturns == null || !returns.__internal__clientReturns.isFuture()) {
      throw new IllegalStateException("Future is not available for this protocol behavior; return type must be Future<T>.");
    }
    final CompletableFuture<T> completableFuture = (CompletableFuture<T>) returns.clientReturns().asFuture();
    try {
      final T outcome = callable.call();
      returns.__internal__outcome = outcome;
      completableFuture.complete(outcome);
    } catch (Exception e) {
      throw new RuntimeException("Actor method returning Future<T> failed.", e);
    }
    return completableFuture;
  }

  /**
   * Answers a {@code CompletesEventually} if the behavior of the currently
   * delivered {@code Message} does answers a {@code Completes<T>}. Otherwise the outcome
   * is undefined.
   * @return CompletesEventually
   */
  protected CompletesEventually completesEventually() {
    return lifeCycle.environment.completesEventually(returns);
  }

  /**
   * Answers the {@code Definition} of this {@code Actor}.
   * @return Definition
   */
  public Definition definition() {
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
    lifeCycle.environment.mailbox.resume(Mailbox.Paused);
  }

  /**
   * Starts the process of stowing messages for this {@code Actor} and registers
   * {@code stowageOverrides} as the protocols that will trigger dispersal.
   * @param stowageOverrides the {@code Class<T>} array of protocols that will trigger dispersal
   */
  protected void stowMessages(final Class<?>... stowageOverrides) {
    lifeCycle.environment.mailbox.suspendExceptFor(Mailbox.Paused, stowageOverrides);
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
    // override for specific recovery

    logger().error("Default before restart recovery after: " + reason.getMessage(), reason);

    lifeCycle.afterStop(this);
  }

  /**
   * The message delivered after the {@code Actor} has been restarted by its supervisor due to an exception.
   * Override to implement.
   * @param reason the {@code Throwable} cause of the supervision restart
   */
  protected void afterRestart(final Throwable reason) {
    // override for specific recovery

    logger().error("Default after restart recovery after: " + reason.getMessage(), reason);

    lifeCycle.beforeStart(this);
  }

  /**
   * The message delivered before the {@code Actor} has been resumed by its supervisor due to an exception.
   * Override to implement.
   * @param reason the {@code Throwable} cause of the supervision resume
   */
  protected void beforeResume(final Throwable reason) {
    // override for specific recovery

    logger().error("Default before resume recovery after: " + reason.getMessage(), reason);
  }
}
