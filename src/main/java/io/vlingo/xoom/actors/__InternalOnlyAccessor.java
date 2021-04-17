// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.Collection;

public class __InternalOnlyAccessor {
  public static Mailbox actorMailbox(final Actor actor) {
    return actor.lifeCycle.environment.mailbox;
  }

  public static Actor actorLookupOrStartThunk(final Stage stage, Definition definition, Address address) {
    return stage.actorLookupOrStart(definition, address);
  }

  public static Actor actorOf(final Stage stage, final Address address) {
    return stage.directory.actorOf(address);
  }

  public static Collection<Address> allActorAddresses(final Stage stage) {
    return stage.directory.addresses();
  }

  public static ClassLoader classLoader(final Stage stage) {
    return stage.world.classLoader();
  }

  public static Actor rawLookupOrStart(final Stage stage, Definition definition, Address address) {
    return stage.rawLookupOrStart(definition, address);
  }
}
