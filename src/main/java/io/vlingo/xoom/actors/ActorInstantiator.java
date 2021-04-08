// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.io.Serializable;

@FunctionalInterface
public interface ActorInstantiator<A extends Actor> extends Serializable {
  A instantiate();

  default void set(final String name, final Object value) { }

  default Class<A> type() {
    return null;
  };
}
