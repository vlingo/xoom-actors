// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

import io.vlingo.common.Completes;

public interface Mailbox extends Runnable {
  void close();
  boolean isClosed();
  boolean isDelivering();
  boolean delivering(final boolean flag);
  void send(final Message message);
  Message receive();
  int pendingMessages();

  default boolean isPreallocated()
    { return false; }
  default void send(final Actor actor, final Class<?> protocol, final Consumer<?> consumer, final Completes<?> completes, final String representation)
    { throw new UnsupportedOperationException("Not a preallocated mailbox."); }
}
