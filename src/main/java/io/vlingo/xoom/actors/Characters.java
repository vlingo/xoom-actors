// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.List;

public class Characters<T> {
  private int current;
  private final T[] states;

  @SuppressWarnings("unchecked")
  public Characters(final List<T> states) {
    this.states = (T[]) states.toArray();
    this.current = 0;
  }

  public final int become(final int which) {
    if (which < 0 || which >= states.length) {
      throw new IndexOutOfBoundsException("Invalid state.");
    }

    if (states[which] == null) {
      throw new IllegalStateException("The state " + which + " is null.");
    }

    final int previous = this.current;
    this.current = which;

    return previous;
  }

  public final T current() {
    return states[current];
  }
}
