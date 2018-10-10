// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.sharedringbuffer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Completes;
import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;

public class SharedRingBufferMailbox implements Mailbox {
  private final AtomicBoolean closed;
  private final Dispatcher dispatcher;
  private final int mailboxSize;
  private final Message[] messages;
  private final AtomicLong sendIndex;
  private final AtomicLong readyIndex;
  private final AtomicLong receiveIndex;

  public void close() {
    if (!closed.get()) {
      closed.set(true);
      dispatcher.close();
    }
  }

  @Override
  public boolean isClosed() {
    return closed.get();
  }

  public boolean isDelivering() {
    throw new UnsupportedOperationException("SharedRingBufferMailbox does not support this operation.");
  }

  public boolean delivering(final boolean flag) {
    throw new UnsupportedOperationException("SharedRingBufferMailbox does not support this operation.");
  }

  @Override
  public boolean isPreallocated() {
    return true;
  }

  public void send(final Message message) {
    throw new UnsupportedOperationException("Use preallocated mailbox send(Actor, ...).");
  }

  @Override
  public void send(final Actor actor, final Class<?> protocol, final Consumer<?> consumer, final Completes<?> completes, final String representation) {
    final long messageIndex = sendIndex.incrementAndGet();
    final int ringSendIndex = (int) (messageIndex % mailboxSize);

    int retries = 0;
    while (ringSendIndex == (int) (receiveIndex.get() % mailboxSize)) {
      if (++retries >= mailboxSize) {
        if (closed.get()) {
          return;
        } else {
          retries = 0;
        }
      }
    }

    messages[ringSendIndex].set(actor, protocol, consumer, completes, representation);

    while (!readyIndex.compareAndSet(messageIndex - 1, messageIndex))
      ;
  }

  public Message receive() {
    final long messageIndex = receiveIndex.get();

    if (messageIndex < readyIndex.get()) {
      final int index = (int) (receiveIndex.incrementAndGet() % mailboxSize);

      return messages[index];
    }

    return null;
  }

  public void run() {
    throw new UnsupportedOperationException("SharedRingBufferMailbox does not support this operation.");
  }

  protected SharedRingBufferMailbox(final Dispatcher dispatcher, final int mailboxSize) {
    this.dispatcher = dispatcher;
    this.mailboxSize = mailboxSize;
    this.closed = new AtomicBoolean(false);
    this.messages = new Message[mailboxSize];
    this.readyIndex = new AtomicLong(-1);
    this.receiveIndex = new AtomicLong(-1);
    this.sendIndex = new AtomicLong(-1);

    initPreallocated();
  }

  private void initPreallocated() {
    for (int idx = 0; idx < mailboxSize; ++idx) {
      messages[idx] = new LocalMessage<>(this);
    }
  }
}
