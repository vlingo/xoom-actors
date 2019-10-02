// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.sharedringbuffer;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.testkit.TestUntil;

public class RingBufferDispatcherTest extends ActorsTest {

  @Test
  public void testClose() throws Exception {
    final TestResults testResults = new TestResults();

    final int mailboxSize = 64;

    final RingBufferDispatcher dispatcher = new RingBufferDispatcher(mailboxSize, 2, false, 4);

    dispatcher.start();

    final Mailbox mailbox = dispatcher.mailbox();

    final CountTakerActor actor = new CountTakerActor(testResults);

    testResults.until = until(mailboxSize);

    for (int count = 1; count <= mailboxSize; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);

      mailbox.send(actor, CountTaker.class, consumer, null, "take(int)");
    }

    testResults.until.completes();

    dispatcher.close();

    final int neverRevieved = mailboxSize * 2;

    for (int count = mailboxSize + 1; count <= neverRevieved; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);

      mailbox.send(actor, CountTaker.class, consumer, null, "take(int)");
    }

    until(0).completes();

    assertEquals(mailboxSize, testResults.highest.get());
  }

  @Test
  public void testBasicDispatch() throws Exception {
    final TestResults testResults = new TestResults();

    final int mailboxSize = 64;

    final RingBufferDispatcher dispatcher = new RingBufferDispatcher(mailboxSize, 2, false, 4);

    dispatcher.start();

    final Mailbox mailbox = dispatcher.mailbox();

    final CountTakerActor actor = new CountTakerActor(testResults);

    testResults.until = until(mailboxSize);

    for (int count = 1; count <= mailboxSize; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);

      mailbox.send(actor, CountTaker.class, consumer, null, "take(int)");
    }

    testResults.until.completes();

    assertEquals(mailboxSize, testResults.highest.get());
  }

  @Test
  public void testOverflowDispatch() throws Exception {
    final TestResults testResults = new TestResults();

    final int mailboxSize = 64;
    final int overflowSize = mailboxSize * 2;

    final RingBufferDispatcher dispatcher = new RingBufferDispatcher(mailboxSize, 2, false, 4);

    final Mailbox mailbox = dispatcher.mailbox();

    final CountTakerActor actor = new CountTakerActor(testResults);

    testResults.until = until(overflowSize);

    for (int count = 1; count <= overflowSize; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);

      mailbox.send(actor, CountTaker.class, consumer, null, "take(int)");
    }

    dispatcher.start();

    testResults.until.completes();

    assertEquals(overflowSize, testResults.highest.get());
  }

  @Test
  public void testNotifyOnSendDispatch() throws Exception {
    final TestResults testResults = new TestResults();

    final int mailboxSize = 64;

    final RingBufferDispatcher dispatcher =
            new RingBufferDispatcher(mailboxSize, 1000, true, 4);

    dispatcher.start();

    final Mailbox mailbox = dispatcher.mailbox();

    final CountTakerActor actor = new CountTakerActor(testResults);

    testResults.until = until(mailboxSize);

    for (int count = 1; count <= mailboxSize; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);

      // notify if in back off
      mailbox.send(actor, CountTaker.class, consumer, null, "take(int)");

      // every third message give time for dispatcher to back off
      if (count % 3 == 0) {
        Thread.sleep(50);
      }
    }

    testResults.until.completes();

    assertEquals(mailboxSize, testResults.highest.get());
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
      if (count > testResults.highest.get()) testResults.highest.set(count);
      testResults.until.happened();
    }
  }

  private static class TestResults {
    public AtomicInteger highest = new AtomicInteger(0);
    public TestUntil until = TestUntil.happenings(0);
  }
}
