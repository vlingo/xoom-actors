// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.testkit;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;

/**
 * Actor that has an immediately delivered mailbox for use in testing.
 * @param <T> the type of actor protocol
 */
public class TestActor<T> implements TestStateView {
  private final Actor actor;
  private final Address address;
  private final T protocolActor;
  private TestContext context;

  /**
   * Construct my default state.
   * @param actor the Actor inside being tested
   * @param protocol the T protocol being tested
   * @param address the Address of the actor
   */
  public TestActor(final Actor actor, final T protocol, final Address address) {
    this.actor = actor;
    this.protocolActor = protocol;
    this.address = address;
    this.context = new TestContext();

    this.actor.viewTestStateInitialization(context);
  }

  /**
   * Answer my {@code actor} inside as the {@code T} protocol.
   * @return T
   */
  public T actor() {
    return protocolActor;
  }

  /**
   * Answer my {@code actor} inside as protocol {@code O}.
   * @param <O> the O protocol of my actor inside
   * @return O
   */
  @SuppressWarnings("unchecked")
  public <O> O actorAs() {
    return (O) protocolActor;
  }

  /**
   * Answer my {@code address}, which is the {@code Address} of my {@code actor} inside.
   * @return Address
   */
  public Address address() {
    return address;
  }

  /**
   * Answer my {@code actor} inside.
   * @return Actor
   */
  public Actor actorInside() {
    return actor;
  }

  /**
   * Answer my {@code context}.
   * @return TestContext
   */
  public TestContext context() {
    return context;
  }

  /**
   * Answer my {@code context} after resetting the expected completions/happenings.
   * @param times the int number of expected completions/happenings
   * @return TestContext
   */
  public TestContext andNowCompleteWithHappenings(final int times) {
    context = context.resetAfterCompletingTo(times);
    return context;
  }

  /**
   * Answer the {@code V} typed value of my {@code context} when it is available.
   * Block unless the value is immediately available.
   * @param <V> the value type
   * @return V
   */
  public <V> V mustComplete() {
    return context.mustComplete();
  }

  /**
   * Answer the {@code TestState} of my {@code actor} inside.
   * @return TestState
   */
  @Override
  public TestState viewTestState() {
    return actor.viewTestState();
  }
}
