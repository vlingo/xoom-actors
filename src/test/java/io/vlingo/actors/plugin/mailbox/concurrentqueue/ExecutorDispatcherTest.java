// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import io.vlingo.actors.Logger;
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

    final TestMailbox mailbox = new TestMailbox(testResults, world.defaultLogger());

    final CountTakerActor actor = new CountTakerActor(testResults, world.defaultLogger());
    
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

    for (int idx = 0; idx < testResults.counts.size(); ++idx) {
      assertTrue(testResults.counts.contains(idx));
    }
  }

  @Test
  public void testExecute() throws Exception {
    final TestResults testResults = new TestResults();
    
    final TestMailbox mailbox = new TestMailbox(testResults, world.defaultLogger());

    final CountTakerActor actor = new CountTakerActor(testResults, world.defaultLogger());
    
    testResults.until = until(Total);
    
    for (int count = 0; count < Total; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
      mailbox.send(message);
      dispatcher.execute(mailbox);
    }
    
    testResults.until.completes();
    
    for (int idx = 0; idx < testResults.counts.size(); ++idx) {
      assertTrue(testResults.counts.contains(idx));
    }
  }
  
  @Test
  public void testRequiresExecutionNotification() throws Exception {
    assertFalse(dispatcher.requiresExecutionNotification());
  }
  
  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();

    dispatcher = new ExecutorDispatcher(1, 1.0f);
  }

  public static class TestMailbox implements Mailbox {
    private final Logger logger;
    private final Queue<Message> queue = new ConcurrentLinkedQueue<>();
    private final TestResults testResults;

    public TestMailbox(final TestResults testResults, final Logger logger) {
      this.testResults = testResults;
      this.logger = logger;
    }

    @Override
    public void run() {
      final Message message = receive();
      if (testResults.log.get()) {
        logger.log("TestMailbox: run: received: " + message);
      }
      if (message != null) {
        message.deliver();
        if (testResults.log.get()) {
          logger.log("TestMailbox: run: adding: " + testResults.highest.get());
        }
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
    public void resume() {
      throw new UnsupportedOperationException("ExecutorDispatcherTest does not support this operation");
    }

    @Override
    public void send(final Message message) {
      queue.add(message);
    }

    @Override
    public void suspendExceptFor(final Class<?>... overrides) {
      throw new UnsupportedOperationException("ExecutorDispatcherTest does not support this operation");
    }

    @Override
    public boolean isSuspended() {
      return false;
    }

    @Override
    public Message receive() {
      return queue.poll();
    }

    /* @see io.vlingo.actors.Mailbox#pendingMessages() */
    @Override
    public int pendingMessages() {
      throw new UnsupportedOperationException("ExecutorDispatcherTest does not support this operation");
    }
  }
  
  public static interface CountTaker {
    void take(final int count);
  }
  
  public static class CountTakerActor extends Actor implements CountTaker {
    private final Logger logger;
    private final TestResults testResults;

    public CountTakerActor(final TestResults testResults, final Logger logger) {
      this.testResults = testResults;
      this.logger = logger;
    }
    
    @Override
    public void take(final int count) {
      if (testResults.log.get()) {
        logger.log("CountTakerActor: take: " + count);
      }
      if (count > testResults.highest.get()) {
        if (testResults.log.get()) {
          logger.log("CountTakerActor: take: " + count + " > " + testResults.highest.get());
        }
        testResults.highest.set(count);
      }
      testResults.counts.add(testResults.highest.get());
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
