// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;
import io.vlingo.actors.MessageTest;

public class ConcurrentQueueMailboxTest extends ActorsTest {
  private static int Total = 10_000;
  
  private Dispatcher dispatcher;
  private Mailbox mailbox;
  
  @Test
  public void testMailboxSendReceive() throws Exception {
    final Actor actor = new NotEvenAnActor();
    final Method method = NotEvenAnActor.class.getMethod("take", new Class[] {int.class});
    
    for (int count = 0; count < Total; ++count) {
      final Message message = MessageTest.testMessageFrom(actor, method, new Object[] { count });
      mailbox.send(message);
    }
    
    pause();
    
    for (int idx = 0; idx < Total; ++idx) {
      assertEquals(idx, (int) NotEvenAnActor.counts.get(idx));
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
  
  public static class NotEvenAnActor extends Actor {
    public static final List<Integer> counts = new ArrayList<>();
    
    public void take(final int count) {
      counts.add(count);
    }
  }
}
