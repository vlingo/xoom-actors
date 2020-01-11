// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.agronampscarrayqueue;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.testkit.TestUntil;

public class ManyToOneConcurrentArrayQueueDispatcherTest extends ActorsTest {
  private static final int MailboxSize = 64;

  @Test
  public void testClose() throws Exception {
    final ManyToOneConcurrentArrayQueueDispatcher dispatcher =
            new ManyToOneConcurrentArrayQueueDispatcher(MailboxSize, 2, false, 4, 10);

    dispatcher.start();

    final Mailbox mailbox = dispatcher.mailbox();

    final CountTakerActor actor = new CountTakerActor();

    actor.until = until(MailboxSize);

    for (int count = 1; count <= MailboxSize; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");

      mailbox.send(message);
    }

    actor.until.completes();

    dispatcher.close();

    final int neverRevieved = MailboxSize * 2;

    for (int count = MailboxSize + 1; count <= neverRevieved; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");

      mailbox.send(message);
    }

    until(0).completes();

    assertEquals(MailboxSize, actor.highest.get());
  }

  @Test
  public void testBasicDispatch() throws Exception {
    final int mailboxSize = 64;

    final ManyToOneConcurrentArrayQueueDispatcher dispatcher =
            new ManyToOneConcurrentArrayQueueDispatcher(mailboxSize, 2, false, 4, 10);

    dispatcher.start();

    final Mailbox mailbox = dispatcher.mailbox();

    final CountTakerActor actor = new CountTakerActor();

    actor.until = until(MailboxSize);

    for (int count = 1; count <= mailboxSize; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");

      mailbox.send(message);
    }

    actor.until.completes();

    assertEquals(mailboxSize, actor.highest.get());
  }

  @Test
  public void testNotifyOnSendDispatch() throws Exception {
    final int mailboxSize = 64;

    final ManyToOneConcurrentArrayQueueDispatcher dispatcher =
            new ManyToOneConcurrentArrayQueueDispatcher(mailboxSize, 1000, true, 4, 10);

    dispatcher.start();

    final Mailbox mailbox = dispatcher.mailbox();

    final CountTakerActor actor = new CountTakerActor();

    actor.until = until(MailboxSize);

    for (int count = 1; count <= mailboxSize; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, CountTaker.class, consumer, "take(int)");

      // notify if in back off
      mailbox.send(message);

      // every third message give time for dispatcher to back off
      if (count % 3 == 0) {
        Thread.sleep(50);
      }
    }

    actor.until.completes();

    assertEquals(mailboxSize, actor.highest.get());
  }

  public static interface CountTaker {
    void take(final int count);
  }

  public static class CountTakerActor extends Actor implements CountTaker {
    public AtomicInteger highest = new AtomicInteger(0);
    public TestUntil until = TestUntil.happenings(0);

    public CountTakerActor() { }

    @Override
    public void take(final int count) {
      if (count > highest.get()) {
        highest.set(count);
      }
      until.happened();
    }
  }
}
