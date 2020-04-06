// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.sharedringbuffer;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.common.SerializableConsumer;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class RingBufferDispatcherTest extends ActorsTest {

  @Test
  public void testClose() throws Exception {
    final int mailboxSize = 64;
    final TestResults testResults = new TestResults(mailboxSize);

    final RingBufferDispatcher dispatcher = new RingBufferDispatcher(mailboxSize, 2, false, 4);

    dispatcher.start();

    final Mailbox mailbox = dispatcher.mailbox();

    final CountTakerActor actor = new CountTakerActor(testResults);

    for (int count = 1; count <= mailboxSize; ++count) {
      final int countParam = count;
      final SerializableConsumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);

      mailbox.send(actor, CountTaker.class, consumer, null, "take(int)");
    }

    assertEquals(mailboxSize, testResults.getHighest());

    dispatcher.close();

    final int neverReviewed = mailboxSize * 2;

    for (int count = mailboxSize + 1; count <= neverReviewed; ++count) {
      final int countParam = count;
      final SerializableConsumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);

      mailbox.send(actor, CountTaker.class, consumer, null, "take(int)");
    }

    assertEquals(mailboxSize, testResults.getHighest());
  }

  @Test
  public void testBasicDispatch() throws Exception {
    final int mailboxSize = 64;
    final TestResults testResults = new TestResults(mailboxSize);

    final RingBufferDispatcher dispatcher = new RingBufferDispatcher(mailboxSize, 2, false, 4);

    dispatcher.start();

    final Mailbox mailbox = dispatcher.mailbox();

    final CountTakerActor actor = new CountTakerActor(testResults);

    for (int count = 1; count <= mailboxSize; ++count) {
      final int countParam = count;
      final SerializableConsumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);

      mailbox.send(actor, CountTaker.class, consumer, null, "take(int)");
    }

    assertEquals(mailboxSize, testResults.getHighest());
  }

  @Test
  public void testOverflowDispatch() throws Exception {
    final int mailboxSize = 64;
    final int overflowSize = mailboxSize * 2;
    final TestResults testResults = new TestResults(mailboxSize);

    final RingBufferDispatcher dispatcher = new RingBufferDispatcher(mailboxSize, 2, false, 4);

    final Mailbox mailbox = dispatcher.mailbox();

    final CountTakerActor actor = new CountTakerActor(testResults);

    for (int count = 1; count <= overflowSize; ++count) {
      final int countParam = count;
      final SerializableConsumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);

      mailbox.send(actor, CountTaker.class, consumer, null, "take(int)");
    }

    dispatcher.start();

    assertEquals(overflowSize, testResults.getHighest());
  }

  @Test
  public void testNotifyOnSendDispatch() throws Exception {
    final int mailboxSize = 64;
    final TestResults testResults = new TestResults(mailboxSize);

    final RingBufferDispatcher dispatcher =
            new RingBufferDispatcher(mailboxSize, 1000, true, 4);

    dispatcher.start();

    final Mailbox mailbox = dispatcher.mailbox();

    final CountTakerActor actor = new CountTakerActor(testResults);

    for (int count = 1; count <= mailboxSize; ++count) {
      final int countParam = count;
      final SerializableConsumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);

      // notify if in back off
      mailbox.send(actor, CountTaker.class, consumer, null, "take(int)");

      // every third message give time for dispatcher to back off
      if (count % 3 == 0) {
        Thread.sleep(50);
      }
    }

    assertEquals(mailboxSize, testResults.getHighest());
  }

  public static interface CountTaker {
    void take(final int count);
  }

  public static class CountTakerActor extends Actor implements CountTaker {
    private final TestResults testResults;
    private final CountTaker self;

    public CountTakerActor(final TestResults testResults) {
      this.testResults = testResults;
      this.self = selfAs(CountTaker.class);
    }

    @Override
    public void take(final int count) {
      if (testResults.isHighest(count)) {
        testResults.setHighest(count);
      }
    }
  }

  private static class TestResults {
    private final AccessSafely accessSafely;

    private TestResults(final int happenings) {
      final AtomicInteger highest = new AtomicInteger(0);
      this.accessSafely = AccessSafely
              .afterCompleting(happenings)
              .writingWith("highest", highest::set)
              .readingWith("highest", highest::get)
              .readingWith("isHighest", (Integer count) -> count > highest.get());
    }

    void setHighest(Integer value){
      this.accessSafely.writeUsing("highest", value);
    }

    int getHighest(){
      return this.accessSafely.readFrom("highest");
    }

    boolean isHighest(Integer value){
      return this.accessSafely.readFromNow("isHighest", value);
    }
  }
}
