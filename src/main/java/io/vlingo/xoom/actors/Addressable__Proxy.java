// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public class Addressable__Proxy implements Addressable {
  private final Actor actor;

  public Addressable__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
  }

  @Override
  public Address address() {
    return actor.address();
  }

  @Override
  public LifeCycle lifeCycle() {
    return actor.lifeCycle;
  }
}
