// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.agronampscarrayqueue;

import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;

public class ManyToOneConcurrentArrayQueueMailbox implements Mailbox {
  private final Dispatcher dispatcher;
  private final ManyToOneConcurrentArrayQueue<Message> queue;
  private final int totalSendRetries;

  @Override
  public void close() {
    dispatcher.close();
    queue.clear();
  }

  @Override
  public boolean isClosed() {
    return dispatcher.isClosed();
  }

  @Override
  public boolean isDelivering() {
    throw new UnsupportedOperationException("ManyToOneConcurrentArrayQueueMailbox does not support this operation.");
  }

  @Override
  public boolean delivering(final boolean flag) {
    throw new UnsupportedOperationException("ManyToOneConcurrentArrayQueueMailbox does not support this operation.");
  }

  @Override
  public void run() {
    throw new UnsupportedOperationException("ManyToOneConcurrentArrayQueueMailbox does not support this operation.");
  }

  @Override
  public void send(final Message message) {
    for (int tries = 0; tries < totalSendRetries; ++tries) {
      if (queue.offer(message)) {
        return;
      }
    }
    throw new IllegalStateException("Count not enqueue message due to busy mailbox.");
  }

  @Override
  public final Message receive() {
    return queue.poll();
  }

  protected ManyToOneConcurrentArrayQueueMailbox(final Dispatcher dispatcher, final int mailboxSize, final int totalSendRetries) {
    this.dispatcher = dispatcher;
    this.queue = new ManyToOneConcurrentArrayQueue<>(mailboxSize);
    this.totalSendRetries = totalSendRetries;
  }
}
