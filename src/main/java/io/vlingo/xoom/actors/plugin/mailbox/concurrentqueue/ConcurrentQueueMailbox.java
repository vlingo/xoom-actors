// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue;

import io.vlingo.xoom.actors.Dispatcher;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.Message;
import io.vlingo.xoom.actors.ResumingMailbox;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ConcurrentQueueMailbox implements Mailbox, Runnable {
  private AtomicBoolean delivering;
  private final Dispatcher dispatcher;
  private AtomicReference<SuspendedDeliveryOverrides> suspendedDeliveryOverrides;
  private AtomicReference<SuspendedDeliveryQueue> suspendedDeliveryQueue;
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
    if (isSuspendedExceptFor(message)) {
      suspendedDeliveryQueue.get().add(message);
    } else {
      queue.add(message);
    }
    if (!isDelivering()) {
      dispatcher.execute(this);
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
    return !suspendedDeliveryOverrides.get()
        .find(name).isEmpty();
  }

  private boolean isSuspendedExceptFor(final Message override) {
    return isSuspended() && suspendedDeliveryOverrides.get().matchesTop(override.protocol());
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
          Message message = suspendedDeliveryQueue.get().poll();
          if (message != null) {
            message.deliver();
          } else {
            break;
          }
        } else {
          final Message message = receive();
          if (message != null) {
            message.deliver();
          } else {
            break;
          }
        }
      }
      delivering.set(false);
      if (!queue.isEmpty() || !suspendedDeliveryQueue.get().isEmpty()) {
        dispatcher.execute(this);
      }
    }
  }

  /* @see io.vlingo.xoom.actors.Mailbox#pendingMessages() */
  @Override
  public int pendingMessages() {
    return queue.size();
  }

  protected ConcurrentQueueMailbox(final Dispatcher dispatcher, final int throttlingCount) {
    this.dispatcher = dispatcher;
    this.delivering = new AtomicBoolean(false);
    this.suspendedDeliveryOverrides = new AtomicReference<>(new SuspendedDeliveryOverrides());
    this.suspendedDeliveryQueue = new AtomicReference<>(new SuspendedDeliveryQueue());
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
      final Overrides overrides = peek();
      if (overrides != null) {
        for (final Class<?> type : overrides.types) {
          if (messageType == type) {
            return true;
          }
        }
      }
      return false;
    }

    Overrides peek() {
      int retries = 0;
      while (true) {
        if (accessible.compareAndSet(false, true)) {
          Overrides temp = null;
          if (!isEmpty()) {
            temp = overrides.get(0);
          }
          accessible.set(false);
          return temp;
        } else {
          if (++retries > 100_000_000) {
            (new Exception()).printStackTrace();
            return null;
          }
        }
      }
    }

    List<Overrides> find(final String name) {
      int retries = 0;
      while (true) {
        if (accessible.compareAndSet(false, true)) {
          List<Overrides> overridesNamed = this.overrides.stream()
              .filter(o -> o.name.equals(name))
              .collect(Collectors.toCollection(ArrayList::new));

          accessible.set(false);
          return overridesNamed;
        } else {
          if (++retries > 100_000_000) {
            (new Exception()).printStackTrace();
            return Collections.emptyList();
          }
        }
      }
    }

    boolean pop(final String name) {
      boolean popped = false;
      int retries = 0;
      while (true) {
        if (accessible.compareAndSet(false, true)) {
          int elements = overrides.size();
          for (int index = 0; index < elements; ++index) {
            if (name.equals(overrides.get(index).name)) {
              if (index == 0) {
                overrides.remove(index);
                popped = true;
                --elements;
                while (index < elements) {
                  if (overrides.get(index).obsolete) {
                    overrides.remove(index);
                    --elements;
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
        } else {
          if (++retries > 100_000_000) {
            (new Exception()).printStackTrace();
            return false;
          }
        }

      }
      return popped;
    }

    void push(final Overrides overrides) {
      int retries = 0;
      while (true) {
        if (accessible.compareAndSet(false, true)) {
          this.overrides.add(overrides);
          accessible.set(false);
          break;
        } else {
          if (++retries > 100_000_000) {
            (new Exception()).printStackTrace();
            return;
          }
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

  private static class SuspendedDeliveryQueue {
    private final AtomicBoolean accessible = new AtomicBoolean(false);
    private final LinkedList<Message> queue = new LinkedList<>();

    public void add(final Message message) {
      while(true) {
        if (accessible.compareAndSet(false, true)) {
          queue.add(message);
          accessible.set(false);
          break;
        }
      }

    }

    public Message poll() {
      while(true) {
        if (accessible.compareAndSet(false, true)) {
          Message message = null;
          if (!queue.isEmpty()) {
            message = queue.pop();
          }
          accessible.set(false);
          return message;
        }
      }
    }

    public boolean isEmpty() {
      return queue.isEmpty();
    }
  }
}
