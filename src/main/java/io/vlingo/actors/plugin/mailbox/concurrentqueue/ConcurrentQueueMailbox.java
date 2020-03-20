// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;
import io.vlingo.actors.ResumingMailbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    return !suspendedDeliveryOverrides.get()
        .find(name).isEmpty();
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
}
