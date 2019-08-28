// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

import io.vlingo.common.Completes;

public class StowedLocalMessage<T> extends LocalMessage<T> {
  public StowedLocalMessage(final Actor actor, final Class<T> protocol, final Consumer<T> consumer, final Returns<?> completes, final String representation) {
    super(actor, protocol, consumer, completes, representation);
  }

  public StowedLocalMessage(final LocalMessage<T> message) {
    super(message);
  }

  @Override
  public boolean isStowed() {
    return true;
  }
}
