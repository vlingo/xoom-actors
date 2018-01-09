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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;
import io.vlingo.actors.MessageTest;

public class ExecutorDispatcherTest extends ActorsTest {
  private static int Total = 10_000;
  
  private Dispatcher dispatcher;
  private Mailbox mailbox;

  @Test
  public void testClose() throws Exception {
    for (int count = 0; count < 3; ++count) {
      mailbox.send(MessageTest.testMessageFrom(null, null, new Integer[] { count }));
      dispatcher.execute(mailbox);
    }

    dispatcher.close();
    
    mailbox.send(MessageTest.testMessageFrom(null, null, new Integer[] { 10 }));
    
    dispatcher.execute(mailbox);
    
    pause();
    
    assertEquals(0, (int) ((TestMailbox) mailbox).counts.get(0));
    assertEquals(1, (int) ((TestMailbox) mailbox).counts.get(1));
    assertEquals(2, (int) ((TestMailbox) mailbox).counts.get(2));
    
    assertEquals(3, ((TestMailbox) mailbox).counts.size());
  }

  @Test
  public void testExecute() throws Exception {
    for (int count = 0; count < Total; ++count) {
      mailbox.send(MessageTest.testMessageFrom(null, null, new Integer[] { count }));
      dispatcher.execute(mailbox);
    }
    
    pause();
    
    for (int idx = 0; idx < Total; ++idx) {
      assertEquals(idx, (int) ((TestMailbox) mailbox).counts.get(idx));
    }
  }
  
  @Test
  public void testRequiresExecutionNotification() throws Exception {
    assertFalse(dispatcher.requiresExecutionNotification());
  }
  
  @Before
  public void setUp() {
    dispatcher = new ExecutorDispatcher(1, 1.0f);
    mailbox = new TestMailbox();
  }
  
  public static class TestMailbox implements Mailbox {
    public final List<Integer> counts = new ArrayList<>();
    private final Queue<Message> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void run() {
      final Message message = receive();
      
      if (message != null) {
        counts.add((Integer) message.args[0]);
      }
    }

    @Override
    public void close() {
      
    }

    @Override
    public boolean isClosed() {
      return false;
    }

    @Override
    public boolean isDelivering() {
      return false;
    }

    @Override
    public boolean delivering(final boolean flag) {
      return flag;
    }

    @Override
    public void send(final Message message) {
      queue.add(message);
    }

    @Override
    public Message receive() {
      return queue.poll();
    }
  }
}
