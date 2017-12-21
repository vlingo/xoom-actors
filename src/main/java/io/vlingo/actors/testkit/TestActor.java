// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.testkit;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;

public class TestActor<T> implements TestStateView {
  private final Actor actor;
  private final Address address;
  private final T protocolActor;

  public TestActor(final Actor actor, final T protocol, final Address address) {
    this.actor = actor;
    this.protocolActor = protocol;
    this.address = address;
  }

  public T actor() {
    return protocolActor;
  }

  public Address address() {
    return address;
  }

  @Override
  public TestState viewTestState() {
    return actor.viewTestState();
  }
}
