// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.ListIterator;

import io.vlingo.actors.testkit.TestState;
import io.vlingo.actors.testkit.TestStateView;

public abstract class Actor implements Stoppable, TestStateView {
  private static final byte FLAG_RESET = 0x00;
  private static final byte FLAG_STOPPED = 0x01;
  private static final byte FLAG_SECURED = 0x02;
  
  private final Environment environment;
  private byte flags;

  public Address address() {
    return environment.address;
  }

  @Override
  public boolean isStopped() {
    return __internalOnlyIsStopped();
  }

  @Override
  public void stop() {
    if (!isStopped()) {
      final String name = environment.definition.actorName();
      if (name != null && !name.equals(World.DEADLETTERS_NAME)) {
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
    if (__internalOnlyIsSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its definition.");
    }
    return __internalOnlyDefinition();
  }

  protected Actor parent() {
    if (__internalOnlyIsSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its parent.");
    }
    return __internalOnlyParent();
  }

  protected void secure() {
    __internalOnlySetSecured();
  }

  protected <T extends Object> T selfAs(final Class<T> protocol) {
    return ActorProxy.createFor(protocol, this, environment.mailbox);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected OutcomeInterest selfAsOutcomeInterest(final Object reference) {
    final OutcomeAware outcomeAware = ActorProxy.createFor(OutcomeAware.class, this, environment.mailbox);
    return new OutcomeInterestActorProxy(outcomeAware, reference);
  }

  protected final Stage stage() {
    if (__internalOnlyIsSecured()) {
      throw new IllegalStateException("A secured actor cannot provide its stage.");
    }
    return __internalOnlyStage();
  }

  protected Stage stageNamed(final String name) {
    return __internalOnlyStage().world().stageNamed(name);
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
    this.environment = ActorFactory.threadLocalEnvironment.get();
    ActorFactory.threadLocalEnvironment.set(null);
    this.flags = FLAG_RESET;
  }

  protected void __internalOnlyAddChild(final Actor child) {
    environment.children.add(child);
  }

  protected Definition __internalOnlyDefinition() {
    return environment.definition;
  }

  protected Environment __internalOnlyEnvironment() {
    return environment;
  }

  protected Actor __internalOnlyParent() {
    return environment.parent;
  }

  protected final Stage __internalOnlyStage() {
    return environment.stage;
  }

  protected void __internalOnlyStop() {
    __internalOnlyStopChildren();

    __internalOnlySetStopped();

    __internalOnlyAfterStop();
  }

  private void __internalOnlyAfterStop() {
    try {
      afterStop();
    } catch (Throwable t) {
      // TODO: Log
      // TODO: Supervise
      System.out.println("vlingo/actors: Actor afterStop() failed: " + t.getMessage());
      t.printStackTrace();
    }
  }

  protected void __internalOnlyBeforeStart() {
    try {
      beforeStart();
    } catch (Throwable t) {
      // TODO: Log
      // TODO: Supervise
      System.out.println("vlingo/actors: Actor beforeStart() failed: " + t.getMessage());
      t.printStackTrace();
    }
  }

  private boolean __internalOnlyIsSecured() {
    return (flags & FLAG_SECURED) == FLAG_SECURED;
  }

  private void __internalOnlySetSecured() {
    flags |= FLAG_SECURED;
  }

  private boolean __internalOnlyIsStopped() {
    return (flags & FLAG_STOPPED) == FLAG_STOPPED;
  }

  private void __internalOnlySetStopped() {
    flags |= FLAG_STOPPED;
  }

  private void __internalOnlyStopChildren() {
    final ListIterator<Actor> iterator = environment.children.listIterator();
    while (iterator.hasNext()) {
      final Actor child = iterator.next();
      iterator.remove();
      child.selfAs(Stoppable.class).stop();
    }
  }
}
