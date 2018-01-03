// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.lang.reflect.Method;

import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestStateView;

public abstract class Actor implements Stoppable, TestStateView {
  private final Environment environment;

  public Address address() {
    return environment.address;
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
    return environment.stage.actorFor(definition, protocol, this);
  }

  protected Definition definition() {
    if (environment.isSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its definition.");
    }
    return environment.definition;
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
    return environment.stage.createActorFor(protocol, this, environment.mailbox);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected OutcomeInterest selfAsOutcomeInterest(final Object reference) {
    final OutcomeAware outcomeAware = environment.stage.createActorFor(OutcomeAware.class, this, environment.mailbox);
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
  // life cycle overrides
  //=======================================

  protected void beforeStart() {
    // override
  }

  protected void afterStop() {
    // override
  }

  protected void beforeRestart(final Exception reason) {
    // override
    afterStop();
  }

  protected void afterRestart(final Exception reason) {
    // override
    beforeStart();
  }

  //=======================================
  // internal implementation
  //=======================================

  protected Actor() {
    final Environment maybeEnvironment = ActorFactory.threadLocalEnvironment.get();
    this.environment = maybeEnvironment != null ? maybeEnvironment : new Environment();
    ActorFactory.threadLocalEnvironment.set(null);
    __internal__SendBeforeStart();
  }

  protected Environment __internal__Environment() {
    return environment;
  }

  protected void __internal__Stop() {
    environment.stopChildren();

    environment.setStopped();

    __internal__AfterStop();
  }

  private void __internal__AfterStop() {
    try {
      afterStop();
    } catch (Throwable t) {
      // TODO: Log
      // TODO: Supervise
      System.out.println("vlingo/actors: Actor afterStop() failed: " + t.getMessage());
      t.printStackTrace();
    }
  }

  protected void __internal__BeforeStart() {
    try {
      beforeStart();
    } catch (Throwable t) {
      // TODO: Log
      // TODO: Supervise
      System.out.println("vlingo/actors: Actor beforeStart() failed: " + t.getMessage());
      t.printStackTrace();
    }
  }

  private void __internal__SendBeforeStart() {
    try {
	  final Method method = Actor.class.getDeclaredMethod("__internal__BeforeStart", new Class[] {});
	  final Message message = new Message(this, method, new Object[] { });
      environment.mailbox.send(message);
    } catch (Exception e) {
      __internal__BeforeStart();
    }
  }
}
