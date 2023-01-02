// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue;

import io.vlingo.xoom.actors.*;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.common.SerializableConsumer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.junit.Assert.*;

public class ExecutorDispatcherTest extends ActorsTest {
  private static int Total = 10_000;

  private Dispatcher dispatcher;

  @Test
  public void testClose() {
    final TestResults testResults = new TestResults(3, false);

    final TestMailbox mailbox = new TestMailbox(testResults, world.defaultLogger());

    final CountTakerActor actor = new CountTakerActor(testResults, world.defaultLogger());

    for (int count = 0; count < 3; ++count) {
      final int countParam = count;
      final SerializableConsumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
      mailbox.send(message);
      dispatcher.execute(mailbox);
    }

    dispatcher.close();

    final SerializableConsumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(10);
    final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");

    mailbox.send(message);

    dispatcher.execute(mailbox);

    final List<Integer> counts = testResults.getCounts();
    assertEquals(3, counts.size());

    for (int idx = 0; idx < counts.size(); ++idx) {
      assertTrue(counts.contains(idx));
    }
  }

  @Test
  public void testExecute() {
    final TestResults testResults = new TestResults(Total, false);

    final TestMailbox mailbox = new TestMailbox(testResults, world.defaultLogger());

    final CountTakerActor actor = new CountTakerActor(testResults, world.defaultLogger());

    for (int count = 0; count < Total; ++count) {
      final int countParam = count;
      final SerializableConsumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
      mailbox.send(message);
      dispatcher.execute(mailbox);
    }

    final List<Integer> counts = testResults.getCounts();
    assertEquals(Total, counts.size());

    for (int idx = 0; idx < counts.size(); ++idx) {
      assertTrue(counts.contains(idx));
    }
  }

  @Test
  public void testRequiresExecutionNotification() {
    assertFalse(dispatcher.requiresExecutionNotification());
  }

  @Test
  public void testThatPoolSizeSet() {
    final Dispatcher dispatcher1 = new ExecutorDispatcher(1, 10, 0);
    final Dispatcher dispatcher2 = new ExecutorDispatcher(1, 8, 20.5f);
    final Dispatcher dispatcher3 = new ExecutorDispatcher(1, 5, 10.0f);

    assertEquals(10, dispatcher1.concurrencyCapacity());
    assertEquals(8, dispatcher2.concurrencyCapacity());
    assertEquals(5, dispatcher3.concurrencyCapacity());
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();

    dispatcher = new ExecutorDispatcher(1, 0, 1.0f);
  }

  private static class TestMailbox implements Mailbox {
    private final Logger logger;
    private final Queue<Message> queue = new ConcurrentLinkedQueue<>();
    private final TestResults testResults;

    private TestMailbox(final TestResults testResults, final Logger logger) {
      this.testResults = testResults;
      this.logger = logger;
    }

    @Override
    public void run() {
      final Message message = receive();
      if (testResults.shouldLog) {
        logger.debug("TestMailbox: run: received: " + message);
      }
      if (message != null) {
        message.deliver();
        if (testResults.shouldLog) {
          logger.debug("TestMailbox: run: adding: " + testResults.getHighest());
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
    public int concurrencyCapacity() {
      return 1;
    }

    @Override
    public void resume(final String name) {
      throw new UnsupportedOperationException("ExecutorDispatcherTest does not support this operation");
    }

    @Override
    public void send(final Message message) {
      queue.add(message);
    }

    @Override
    public void suspendExceptFor(final String name, final Class<?>... overrides) {
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

    /* @see io.vlingo.xoom.actors.Mailbox#pendingMessages() */
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
      testResults.take(count, this.logger);
    }
  }

  private static class TestResults {
    private final AccessSafely accessSafely;
    private final boolean shouldLog;

    private TestResults(final int happenings, boolean shouldLog) {
      final List<Integer> counts = new CopyOnWriteArrayList<>();
      final AtomicInteger highest = new AtomicInteger(0);
      this.shouldLog = shouldLog;
      this.accessSafely = AccessSafely
              .afterCompleting(happenings)
              .writingWith("results", (BiConsumer<Integer, Logger>) (count, logger) -> {
                if (shouldLog) {
                  logger.debug("CountTakerActor: take: " + count);
                }
                if (count > highest.get()) {
                  if (shouldLog) {
                    logger.debug("CountTakerActor: take: " + count + " > " + highest.get());
                  }
                  highest.set(count);
                }

                counts.add(highest.get());
              })
              .readingWith("results", () -> counts)
              .readingWith("highest", highest::get);
    }

    void take(final Integer count, Logger logger){
      this.accessSafely.writeUsing("results", count, logger);
    }

    List<Integer> getCounts(){
      return this.accessSafely.readFrom("results");
    }

    int getHighest(){
      return this.accessSafely.readFromNow("highest");
    }
  }
}
