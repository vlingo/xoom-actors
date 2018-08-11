// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.List;

public class Characters<T> {
  private final T[] states;
  private int which;

  @SuppressWarnings("unchecked")
  public Characters(final int numberOfStates) {
    this.states = (T[]) new Object[numberOfStates];
    this.which = 0;
  }

  @SuppressWarnings("unchecked")
  public Characters(final List<T> states) {
    this.states = (T[]) states.toArray();
    this.which = 0;
  }

  public final int become(final int which) {
    if (which < 0 || which >= states.length) {
      throw new IndexOutOfBoundsException("Invalid possibility.");
    }

    if (states[which] == null) {
      throw new IllegalStateException("This possibility is null.");
    }

    final int previous = this.which;
    this.which = which;

    return previous;
  }

  public void canBecome(final List<T> states) {
    for (final T state : states) {
      canBecome(state);
    }
  }

  public void canBecome(final T state) {
    for (int which = 0; which < states.length; ++which) {
      if (states[which] == null) {
        canBecome(state, which);
        return;
      }
    }
    throw new IllegalStateException("No remaining possibilities. Use explicit possibleToBecome(possibility, which).");
  }

  public void canBecome(final T state, final int which) {
    if (which < 0 || which >= states.length) {
      throw new IndexOutOfBoundsException("Invalid possibility.");
    }
    states[which] = state;
  }

  public final T current() {
    return states[which];
  }
}
