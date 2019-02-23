// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;

public class ConcurrentQueueMailbox implements Mailbox, Runnable {
  private AtomicBoolean delivering;
  private final Dispatcher dispatcher;
  private AtomicReference<SuspendedDeliveryOverrides> suspendedDeliveryOverrides;
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
  public void resume(final String name) {
    if (suspendedDeliveryOverrides.get().pop(name)) {
      dispatcher.execute(this);
    }
  }

  @Override
  public void send(final Message message) {
    if (isSuspended()) {
      if (suspendedDeliveryOverrides.get().matchesTop(message.protocol())) {
        message.deliver();
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
    this.suspendedDeliveryOverrides = new AtomicReference<>(new SuspendedDeliveryOverrides());
    this.queue = new ConcurrentLinkedQueue<Message>();
    this.throttlingCount = (byte) throttlingCount;
  }

  private static class SuspendedDeliveryOverrides {
    private final AtomicBoolean accessible;
    private final List<Overrides> overrides;

    SuspendedDeliveryOverrides() {
      this.accessible = new AtomicBoolean(false);
      this.overrides = new ArrayList<>(0);
    }

    boolean isEmpty() {
      return overrides.isEmpty();
    }

    boolean matchesTop(final Class<?> messageType) {
      for (final Class<?> type : peek().types) {
        if (messageType == type) {
          return true;
        }
      }
      return false;
    }

    Overrides peek() {
      while (true) {
        if (accessible.compareAndSet(false, true)) {
          if (!isEmpty()) {
            Overrides temp = overrides.get(0);
            accessible.set(false);
            return temp;
          }
        }
      }
    }

    boolean pop(final String name) {
      boolean popped = false;

      while (true) {
        if (accessible.compareAndSet(false, true)) {
          int elements = overrides.size();
          for (int index = 0; index < elements; ++index) {
            if (name.equals(overrides.get(index).name)) {
              if (index == 0) {
                overrides.remove(index);
                popped = true;
                --elements;
                for (int possiblyObsolete = index + 1; possiblyObsolete < elements; ++possiblyObsolete) {
                  if (overrides.get(possiblyObsolete).obsolete) {
                    overrides.remove(index);
                  } else {
                    break;
                  }
                }
              } else {
                overrides.get(index).obsolete = true;
              }
              accessible.set(false);
              break;
            }
          }
          break;
        }
      }
      return popped;
    }

    void push(final Overrides overrides) {
      while (true) {
        if (accessible.compareAndSet(false, true)) {
          this.overrides.add(overrides);
          accessible.set(false);
          break;
        }
      }
    }
  }

  private static class Overrides {
    final String name;
    boolean obsolete;
    final Class<?>[] types;

    Overrides(final String name, final Class<?>[] types) {
      this.name = name;
      this.types = types;
      this.obsolete = false;
    }
  }
}
