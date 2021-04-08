// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public final class DeadLetter {
  public final Actor actor;
  public final String representation;

  public DeadLetter(final Actor actor, final String representation) {
    this.actor = actor;
    this.representation = representation;
  }

  @Override
  public String toString() {
    return "DeadLetter[" + actor + "." + representation + "]";
  }
}
