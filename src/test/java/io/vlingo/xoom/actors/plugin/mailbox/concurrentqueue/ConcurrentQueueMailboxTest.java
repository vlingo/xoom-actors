// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue;

import io.vlingo.xoom.actors.*;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.common.SerializableConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ConcurrentQueueMailboxTest extends ActorsTest {
  private static int Total = 10_000;

  private Dispatcher dispatcher;
  private Mailbox mailbox;

  @Test
  public void testMailboxSendReceive() {
    final TestResults testResults = new TestResults(Total);

    final CountTakerActor actor = new CountTakerActor(testResults);

    for (int count = 0; count < Total; ++count) {
      final int countParam = count;
      final SerializableConsumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
      mailbox.send(message);
    }

    for (int idx = 0; idx < Total; ++idx) {
      assertEquals(idx, (int) actor.testResults.getCount(idx));
    }
  }

  @Test
  public void testThatSuspendResumes(){
      final String paused = "paused#";
      final String exceptional = "exceptional#";

      final Dispatcher dispatcher = new ExecutorDispatcher(1, 0, 1.0f);
      final Mailbox mailbox = new ConcurrentQueueMailbox(dispatcher, 1);

      mailbox.suspendExceptFor(paused, CountTakerActor.class);

      mailbox.suspendExceptFor(exceptional, CountTakerActor.class);

      mailbox.resume(exceptional);

      mailbox.resume(paused);

      assertFalse(mailbox.isSuspended());
  }

  @Test
  public void testThatMessagesAreDeliveredInOrderTheyArrived() {

    final Dispatcher dispatcher = new ExecutorDispatcher(2, 0, 1.0f);
    final Mailbox mailbox = new ConcurrentQueueMailbox(dispatcher, 1);

    final TestResults testResults = new TestResults(3);
    final CountTakerActor actor = new CountTakerActor(testResults);

    for (int count = 0; count < 3; ++count) {
      final int countParam = count;
      final SerializableConsumer<CountTaker> consumer = (consumerActor) -> {
        // Give longer Delay to messages that come first
        delay(20 - (countParam * 10));
        consumerActor.take(countParam);
      };
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
      mailbox.send(message);
    }

    assertEquals(Arrays.asList(0, 1, 2), actor.testResults.getCounts());
  }

  @Test
  public void testThatSuspendedOverrideMessagesAreDeliveredInOrderTheyArrived() {

    final Dispatcher dispatcher = new ExecutorDispatcher(2, 0, 1.0f);
    final Mailbox mailbox = new ConcurrentQueueMailbox(dispatcher, 1);

    final TestResults testResults = new TestResults(3);
    final CountTakerActor actor = new CountTakerActor(testResults);

    mailbox.suspendExceptFor("paused#", CountTaker.class);

    for (int count = 0; count < 3; ++count) {
      final int countParam = count;
      final SerializableConsumer<CountTaker> consumer = (consumerActor) -> {
        // Give longer Delay to messages that come first
        delay(20 - (countParam * 10));
        consumerActor.take(countParam);
      };
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");
      mailbox.send(message);
    }

    assertEquals(Arrays.asList(0, 1, 2), actor.testResults.getCounts());
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();

    dispatcher = new ExecutorDispatcher(1, 0, 1.0f);
    mailbox = new ConcurrentQueueMailbox(dispatcher, 1);
  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();

    mailbox.close();
    dispatcher.close();
  }

  private void delay(final int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
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
      testResults.addCount(count);
    }
  }

  private static class TestResults {
    private final AccessSafely accessSafely;

    private TestResults(final int happenings) {
      final ArrayList<Integer> list = new ArrayList<>();
      this.accessSafely = AccessSafely
              .afterCompleting(happenings)
              .writingWith("counts", (Consumer<Integer>) list::add)
              .readingWith("counts", (Integer index)-> list.get(index))
              .readingWith("counts", () -> list);
    }

    void addCount(Integer i){
      this.accessSafely.writeUsing("counts", i);
    }

    Integer getCount(int index){
      return this.accessSafely.readFrom("counts", index);
    }

    List<Integer> getCounts() {
      return this.accessSafely.readFrom("counts");
    }
  }
}
