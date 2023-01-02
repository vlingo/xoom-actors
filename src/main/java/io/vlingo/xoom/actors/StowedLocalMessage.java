// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import io.vlingo.xoom.common.SerializableConsumer;

public class StowedLocalMessage<T> extends LocalMessage<T> {
  public StowedLocalMessage(final Actor actor, final Class<T> protocol, final SerializableConsumer<T> consumer, final Returns<?> completes, final String representation) {
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
