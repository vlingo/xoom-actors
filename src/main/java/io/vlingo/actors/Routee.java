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
 * Routee
 */
public class Routee {
  
  private final Actor actor;

  public static List<Routee> forAll(List<Actor> children) {
    return children.stream()
      .map(Routee::new)
      .collect(Collectors.toList());
  }

  public Routee(Actor actor) {
    super();
    this.actor = actor;
  }
  
  public int mailboxSize() {
    return actor.lifeCycle.environment.mailbox.pendingMessages();
  }
  
  public <T> T as(Class<T> protocol) {
    return actor.selfAs(protocol);
  }
}
