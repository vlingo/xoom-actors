// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.testkit;

import java.util.Arrays;
import java.util.List;

import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;
import io.vlingo.actors.testkit.TestWorld;

public class TestMailbox implements Mailbox {
  public static final String Name = "testerMailbox";

  private final List<String> lifecycleMessages = Arrays.asList("start", "afterStop", "beforeRestart", "afterRestart");
  private boolean closed;
  private final TestWorld world;

  public TestMailbox() {
    this.world = TestWorld.Instance.get();
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
  public boolean delivering(final boolean flag) {
    throw new UnsupportedOperationException("TestMailbox does not support this operation.");
  }

  @Override
  public void send(final Message message) {
    try {
      if (!message.actor().isStopped()) {
        if (!isLifecycleMessage(message)) {
          world.track(message);
        }
      }

      message.actor().viewTestStateInitialization(null);
      message.deliver();
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage(), t);
    }
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
  
  private boolean isLifecycleMessage(final Message message) {
    final String representation = message.representation();
    final int openParenIndex = representation.indexOf("(");
    return lifecycleMessages.contains(representation.substring(0, openParenIndex));
  }
}
