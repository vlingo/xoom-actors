// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;
import io.vlingo.actors.testkit.TestUntil;

public class ExecutorDispatcherTest extends ActorsTest {
  private static int Total = 10_000;
  
  private Dispatcher dispatcher;
  private Mailbox mailbox;

  @Test
  public void testClose() throws Exception {
    final CountTakerActor actor = new CountTakerActor();
    
    CountTakerActor.instance.until = until(3);
    
    for (int count = 0; count < 3; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
      mailbox.send(message);
      dispatcher.execute(mailbox);
    }

    dispatcher.close();
    
    final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(10);
    final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
    
    mailbox.send(message);
    
    dispatcher.execute(mailbox);
    
    CountTakerActor.instance.until.completes();
    
    assertEquals(0, (int) ((TestMailbox) mailbox).counts.get(0));
    assertEquals(1, (int) ((TestMailbox) mailbox).counts.get(1));
    assertEquals(2, (int) ((TestMailbox) mailbox).counts.get(2));
    
    assertEquals(3, ((TestMailbox) mailbox).counts.size());
  }

  @Test
  public void testExecute() throws Exception {
    final CountTakerActor actor = new CountTakerActor();
    
    CountTakerActor.instance.until = until(Total);
    
    for (int count = 0; count < Total; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
      mailbox.send(message);
      dispatcher.execute(mailbox);
    }
    
    CountTakerActor.instance.until.completes();
    
    for (int idx = 0; idx < Total; ++idx) {
      assertEquals(idx, (int) ((TestMailbox) mailbox).counts.get(idx));
    }
  }
  
  @Test
  public void testRequiresExecutionNotification() throws Exception {
    assertFalse(dispatcher.requiresExecutionNotification());
  }
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
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
        message.deliver();
        counts.add((Integer) CountTakerActor.instance.highest);
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
  
  public static interface CountTaker {
    void take(final int count);
  }
  
  public static class CountTakerActor extends Actor implements CountTaker {
    public static CountTakerActor instance;
    
    public int highest;
    public TestUntil until;

    public CountTakerActor() {
      instance = this;
    }
    
    @Override
    public void take(final int count) {
      if (count > highest) {
        highest = count;
      }
      until.happened();
    }
  }
}
