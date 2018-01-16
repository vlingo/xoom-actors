// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;

public class ConcurrentQueueMailboxTest extends ActorsTest {
  private static int Total = 10_000;
  
  private Dispatcher dispatcher;
  private Mailbox mailbox;
  
  @Test
  public void testMailboxSendReceive() throws Exception {
    final CountTakerActor actor = new CountTakerActor();
    
    for (int count = 0; count < Total; ++count) {
      final int countParam = count;
      final Consumer<CountTaker> consumer = (consumerActor) -> consumerActor.take(countParam);
      final LocalMessage<CountTaker> message = new LocalMessage<CountTaker>(actor, actor, consumer, "take(int)");
      mailbox.send(message);
    }
    
    pause();
    
    for (int idx = 0; idx < Total; ++idx) {
      assertEquals(idx, (int) CountTakerActor.counts.get(idx));
    }
  }
  
  @Before
  public void setUp() {
    ConcurrentQueueMailboxSettings.with(1);
    dispatcher = new ExecutorDispatcher(1, 1.0f);
    mailbox = new ConcurrentQueueMailbox(dispatcher);
  }
  
  @After
  public void tearDown() {
    mailbox.close();
    dispatcher.close();
  }
  
  public static interface CountTaker {
    void take(final int count);
  }
  
  public static class CountTakerActor extends Actor implements CountTaker {
    public static final List<Integer> counts = new ArrayList<>();    
    
    @Override
    public void take(final int count) {
      counts.add(count);
    }
  }
}
