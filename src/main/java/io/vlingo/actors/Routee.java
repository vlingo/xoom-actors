// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.List;
import java.util.stream.Collectors;
/**
 * Routee represents a potential target for for a routed message.
 */
public class Routee {
  
  private final Actor actor;

  static List<Routee> forAll(final List<Actor> children) {
    return children.stream()
      .map(Routee::new)
      .collect(Collectors.toList());
  }

  Routee(final Actor actor) {
    super();
    this.actor = actor;
  }
  
  public int pendingMessages() {
    return actor.lifeCycle.environment.mailbox.pendingMessages();
  }
  
  public <T> T as(final Class<T> protocol) {
    return actor.selfAs(protocol);
  }
}
