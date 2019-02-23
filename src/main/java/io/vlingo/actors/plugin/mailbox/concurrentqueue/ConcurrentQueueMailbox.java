// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;

public class ConcurrentQueueMailbox implements Mailbox, Runnable {
  private AtomicBoolean delivering;
  private final Dispatcher dispatcher;
  private AtomicReference<Stack<List<Class<?>>>> suspendedOverrides;
  private final Queue<Message> queue;
  private final byte throttlingCount;

  @Override
  public void close() {
    queue.clear();
    dispatcher.close();
  }

  @Override
  public boolean isClosed() {
    return dispatcher.isClosed();
  }

  @Override
  public void resume() {
    suspendedOverrides.get().pop();
    dispatcher.execute(this);
  }

  @Override
  public void send(final Message message) {
    if (isSuspended()) {
      final Class<?> messageType = message.protocol();
      for (final Class<?> type : suspendedOverrides.get().peek()) {
        if (messageType == type) {
          message.deliver();
          if (!queue.isEmpty()) {
            dispatcher.execute(this);
          }
          return;
        }
      }
      queue.add(message);
    } else {
      queue.add(message);
      if (!isDelivering()) {
        dispatcher.execute(this);
      }
    }
  }

  @Override
  public void suspendExceptFor(final Class<?>... overrides) {
    suspendedOverrides.get().push(Arrays.asList(overrides));
  }

  @Override
  public boolean isSuspended() {
    return !suspendedOverrides.get().empty();
  }

  @Override
  public Message receive() {
    return queue.poll();
  }

  @Override
  public boolean isDelivering() {
    return delivering.get();
  }

  @Override
  public void run() {
    if (delivering.compareAndSet(false, true)) {
      final int total = (int) throttlingCount;
      for (int count = 0; count < total; ++count) {
        if (isSuspended()) {
          break;
        }
        final Message message = receive();
        if (message != null) {
          message.deliver();
        } else {
          break;
        }
      }
      delivering.set(false);
      if (!queue.isEmpty()) {
        dispatcher.execute(this);
      }
    }
  }
  
  /* @see io.vlingo.actors.Mailbox#pendingMessages() */
  @Override
  public int pendingMessages() {
    return queue.size();
  }
  
  protected ConcurrentQueueMailbox(final Dispatcher dispatcher, final int throttlingCount) {
    this.dispatcher = dispatcher;
    this.delivering = new AtomicBoolean(false);
    this.suspendedOverrides = new AtomicReference<>(new Stack<>());
    this.queue = new ConcurrentLinkedQueue<Message>();
    this.throttlingCount = (byte) throttlingCount;
  }
}
