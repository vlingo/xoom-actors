// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.testkit.AccessSafely;

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
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
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
              .readingWith("counts", (Integer index)-> list.get(index));
    }

    void addCount(Integer i){
      this.accessSafely.writeUsing("counts", i);
    }

    Integer getCount(int index){
      return this.accessSafely.readFrom("counts", index);
    }
  }
}
