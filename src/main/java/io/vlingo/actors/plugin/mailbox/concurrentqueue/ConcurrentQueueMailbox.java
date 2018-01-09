// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;

public class ConcurrentQueueMailbox implements Mailbox, Runnable {
  private AtomicBoolean delivering;
  private final Dispatcher dispatcher;
  private final Queue<Message> queue;

  public void close() {
    queue.clear();
    dispatcher.close();
  }

  @Override
  public boolean isClosed() {
    return dispatcher.isClosed();
  }

  public void send(final Message message) {
    queue.add(message);
    if (!isDelivering()) {
      dispatcher.execute(this);
    }
  }

  public Message receive() {
    return queue.poll();
  }

  public boolean isDelivering() {
    return delivering.get();
  }

  public boolean delivering(final boolean flag) {
    return delivering.compareAndSet(!flag, flag);
  }

  public void run() {
    final int total = ConcurrentQueueMailboxSettings.instance().throttlingCount;
    for (int count = 0; count < total; ++count) {
      final Message message = receive();
      if (message != null) {
        message.deliver();
      } else {
        break;
      }
    }
    delivering(false);
    if (!queue.isEmpty()) {
      dispatcher.execute(this);
    }
  }

  protected ConcurrentQueueMailbox(final Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
    this.delivering = new AtomicBoolean(false);
    this.queue = new ConcurrentLinkedQueue<Message>();
  }
}
