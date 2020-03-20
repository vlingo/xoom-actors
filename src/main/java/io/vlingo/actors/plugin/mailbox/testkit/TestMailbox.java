// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.testkit;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;
import io.vlingo.actors.testkit.TestWorld;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class TestMailbox implements Mailbox {
  public static final String Name = "testerMailbox";

  private final List<String> lifecycleMessages = Arrays.asList("start", "afterStop", "beforeRestart", "afterRestart");
  private boolean closed;
  private final Queue<Message> queue;
  private AtomicReference<Stack<List<Class<?>>>> suspendedOverrides;
  private final TestWorld world;

  public TestMailbox() {
    this.world = TestWorld.Instance.get();
    this.queue = new ConcurrentLinkedQueue<>();
    this.suspendedOverrides = new AtomicReference<>(new Stack<>());
  }

  @Override
  public void run() {
    throw new UnsupportedOperationException("TestMailbox does not support this operation.");
  }

  @Override
  public void close() {
    closed = true;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public boolean isDelivering() {
    throw new UnsupportedOperationException("TestMailbox does not support this operation.");
  }

  @Override
  public int concurrencyCapacity() {
    return 1;
  }

  @Override
  public void resume(final String name) {
    if (!suspendedOverrides.get().empty()) {
      suspendedOverrides.get().pop();
    }
    resumeAll();
  }

  @Override
  public void send(final Message message) {
    try {
      if (!message.actor().isStopped()) {
        if (!isLifecycleMessage(message)) {
          world.track(message);
        }
      }

      if (isSuspended()) {
        queue.add(message);
        return;
      } else {
        resumeAll();
      }

      message.actor().viewTestStateInitialization(null);
      message.deliver();
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage(), t);
    }
  }

  @Override
  public void suspendExceptFor(final String name, final Class<?>... overrides) {
    suspendedOverrides.get().push(Arrays.asList(overrides));
  }

  @Override
  public boolean isSuspended() {
    return !suspendedOverrides.get().empty();
  }

  @Override
  public boolean isSuspendedFor(String name) {
    return isSuspended();
  }

  @Override
  public Message receive() {
    throw new UnsupportedOperationException("TestMailbox does not support this operation.");
  }

  /* @see io.vlingo.actors.Mailbox#pendingMessages() */
  @Override
  public int pendingMessages() {
    throw new UnsupportedOperationException("TestMailbox does not support this operation");
  }

  private void resumeAll() {
    while (!queue.isEmpty()) {
      final Message queued = queue.poll();
      if (queued != null) {
        final Actor actor = queued.actor();
        if (actor != null) {
          actor.viewTestStateInitialization(null);
          queued.deliver();
        }
      }
    }
  }

  private boolean isLifecycleMessage(final Message message) {
    final String representation = message.representation();
    final int openParenIndex = representation.indexOf("(");
    return lifecycleMessages.contains(representation.substring(0, openParenIndex));
  }
}
