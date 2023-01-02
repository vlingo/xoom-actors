// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.xoom.actors.Dispatcher;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.Message;
import io.vlingo.xoom.actors.ResumingMailbox;
import io.vlingo.xoom.actors.plugin.mailbox.SuspendedDeliveryOverrides;
import io.vlingo.xoom.actors.plugin.mailbox.SuspendedDeliveryOverrides.Overrides;

public class ConcurrentQueueMailbox implements Mailbox, Runnable {
  private AtomicBoolean delivering;
  private final Dispatcher dispatcher;
  private AtomicReference<SuspendedDeliveryOverrides> suspendedDeliveryOverrides;
  private final Queue<Message> queue;
  private final byte throttlingCount;

  @Override
  public void close() {
    queue.clear();
  }

  @Override
  public boolean isClosed() {
    return dispatcher.isClosed();
  }

  @Override
  public int concurrencyCapacity() {
    return dispatcher.concurrencyCapacity();
  }

  @Override
  public void resume(final String name) {
    if (suspendedDeliveryOverrides.get().pop(name)) {
      dispatcher.execute(this);
    }
  }

  @Override
  public void send(final Message message) {
    if (isSuspended()) {
      if (suspendedDeliveryOverrides.get().matchesTop(message.protocol())) {
        dispatcher.execute(new ResumingMailbox(message));
        if (!queue.isEmpty()) {
          dispatcher.execute(this);
        }
        return;
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
  public void suspendExceptFor(final String name, final Class<?>... overrides) {
    suspendedDeliveryOverrides.get().push(new Overrides(name, overrides));
  }

  @Override
  public boolean isSuspended() {
    return !suspendedDeliveryOverrides.get().isEmpty();
  }

  @Override
  public boolean isSuspendedFor(String name) {
    return !suspendedDeliveryOverrides.get().find(name).isEmpty();
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
      final int total = throttlingCount;
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

  /* @see io.vlingo.xoom.actors.Mailbox#pendingMessages() */
  @Override
  public int pendingMessages() {
    return queue.size();
  }

  ConcurrentQueueMailbox(final Dispatcher dispatcher, final int throttlingCount) {
    this.dispatcher = dispatcher;
    this.delivering = new AtomicBoolean(false);
    this.suspendedDeliveryOverrides = new AtomicReference<>(new SuspendedDeliveryOverrides());
    this.queue = new ConcurrentLinkedQueue<Message>();
    this.throttlingCount = (byte) throttlingCount;
  }
}
