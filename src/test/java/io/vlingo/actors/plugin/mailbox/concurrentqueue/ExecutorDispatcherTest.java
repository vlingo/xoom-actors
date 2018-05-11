// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

  @Test
  public void testClose() throws Exception {
    final TestResults testResults = new TestResults();

    testResults.log.set(true);

    final TestMailbox mailbox = new TestMailbox(testResults);

    final CountTakerActor actor = new CountTakerActor(testResults);
    
    testResults.until = until(3);
    
    for (int count = 0; count < 3; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
      mailbox.send(message);
      dispatcher.execute(mailbox);
    }

    testResults.until.completes();
    
    dispatcher.close();
    
    final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(10);
    final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
    
    mailbox.send(message);
    
    dispatcher.execute(mailbox);
    
    assertEquals(3, testResults.counts.size());

    assertEquals(0, (int) testResults.counts.get(0));
    assertEquals(1, (int) testResults.counts.get(1));
    assertEquals(2, (int) testResults.counts.get(2));
  }

  @Test
  public void testExecute() throws Exception {
    final TestResults testResults = new TestResults();
    
    final TestMailbox mailbox = new TestMailbox(testResults);

    final CountTakerActor actor = new CountTakerActor(testResults);
    
    testResults.until = until(Total);
    
    for (int count = 0; count < Total; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
      mailbox.send(message);
      dispatcher.execute(mailbox);
    }
    
    testResults.until.completes();
    
    int idx = 0;
    for (final Integer count : testResults.counts) {
      assertEquals(idx++, (int) count);
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
  }

  public static class TestMailbox implements Mailbox {
    private final Queue<Message> queue = new ConcurrentLinkedQueue<>();
    private final TestResults testResults;

    public TestMailbox(final TestResults testResults) {
      this.testResults = testResults;
    }

    @Override
    public void run() {
      final Message message = receive();
if (testResults.log.get()) System.out.println("TestMailbox: run: received: " + message);
      if (message != null) {
        testResults.counts.add(testResults.highest.get());
        message.deliver();
if (testResults.log.get()) System.out.println("TestMailbox: run: adding: " + testResults.highest.get());
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
    private final TestResults testResults;

    public CountTakerActor(final TestResults testResults) {
      this.testResults = testResults;
    }
    
    @Override
    public void take(final int count) {
if (testResults.log.get()) System.out.println("CountTakerActor: take: " + count);
      if (count > testResults.highest.get()) {
if (testResults.log.get()) System.out.println("CountTakerActor: take: " + count + " > " + testResults.highest.get());
        testResults.highest.set(count);
      }
      testResults.until.happened();
    }
  }
  
  private static class TestResults {
    public final AtomicBoolean log = new AtomicBoolean(false);
    public final List<Integer> counts = new CopyOnWriteArrayList<>();
    public AtomicInteger highest = new AtomicInteger(0);
    public TestUntil until = TestUntil.happenings(0);
  }
}
