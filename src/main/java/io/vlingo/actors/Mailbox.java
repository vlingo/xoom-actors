// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

import io.vlingo.common.Completes;

/**
 * Standard actor mailbox protocol.
 */
public interface Mailbox extends Runnable {
  /**
   * Close me.
   */
  void close();

  /**
   * Answer whether or not I am closed.
   * @return boolean
   */
  boolean isClosed();

  /**
   * Answer whether or not I am currently delivering a message.
   * @return boolean
   */
  boolean isDelivering();

  /**
   * Recovers the previous operational mode, either active or suspended,
   * and if active resumes delivery. If the restored operational mode
   * is still suspended, then at least one more {@code resume()} is required.
   */
  void resume();

  /**
   * Arrange for {@code Message} to be sent, which will generally
   * be delivered by another thread. Exceptions to this rule are
   * for possible, such as for {@code TestMailbox}.
   * @param message the Message to send
   */
  void send(final Message message);

  /**
   * Suspend message deliver but allow any of the given {@code overrides}
   * to pass through, essentially giving these priority. Note that the
   * receiving Actor must use {@code resume()} to cause normally delivery
   * when it is ready.
   * <p>
   * NOTE: If {@code suspendExceptFor(overrides)} is used multiple times before
   * the implementing {@code Mailbox} is resumed, the outcome is implementation
   * dependent. The implementor may accumulate or replace its internal
   * overrides with the {@code overrides} parameter.
   * @param overrides the varargs {@code Class<?>} that are allowed to be delivered although others are suspended
   */
  void suspendExceptFor(final Class<?>... overrides);

  /**
   * Answer whether or not I am currently suspended.
   * @return return
   */
  boolean isSuspended();

  /**
   * Answer the next {@code Message} that can be received.
   * @return Message
   */
  Message receive();

  /**
   * Answer the count of messages that have not yet been delivered.
   * @return int
   */
  int pendingMessages();

  /**
   * Answer whether or not I am a {@code Mailbox} with pre-allocated and reusable {@code Message} elements.
   * @return boolean
   */
  default boolean isPreallocated()
    { return false; }

  /**
   * Arrange for {@code Message} to be sent by setting the pre-allocated
   * and reusable element with the parameters. This manner of sending
   * is meant to be used only when {@code isPreallocated()} answers {@code true}.
   * @param actor the Actor being sent the message
   * @param protocol the {@code Class<?>} type of Actor protocol
   * @param consumer the {@code Consumer<?>} to carry out the action
   * @param completes the {@code Completes<?>} through which return values are communicated; null if void return
   * @param representation the String representation of this message invocation
   */
  default void send(final Actor actor, final Class<?> protocol, final Consumer<?> consumer, final Completes<?> completes, final String representation)
    { throw new UnsupportedOperationException("Not a preallocated mailbox."); }
}
