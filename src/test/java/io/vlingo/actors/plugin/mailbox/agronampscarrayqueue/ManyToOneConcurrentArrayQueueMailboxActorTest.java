// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.agronampscarrayqueue;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Definition;
import io.vlingo.actors.plugin.PluginProperties;

public class ManyToOneConcurrentArrayQueueMailboxActorTest extends ActorsTest {
  private static final int MailboxSize = 64;
  private static final int MaxCount = 1024;
  
  @Test
  public void testBasicDispatch() throws Exception {
    final CountTaker countTaker =
            world.actorFor(
                    Definition.has(CountTakerActor.class, Definition.NoParameters, "testRingMailbox", "countTaker-1"),
                    CountTaker.class);
    
    final int totalCount = MailboxSize / 2;
    
    for (int count = 1; count <= totalCount; ++count) {
      countTaker.take(count);
    }
    
    pause();
    
    assertEquals(MaxCount, CountTakerActor.highest);
  }

  @Test
  public void testOverflowDispatch() throws Exception {
    final CountTaker countTaker =
            world.actorFor(
                    Definition.has(CountTakerActor.class, Definition.NoParameters, "testRingMailbox", "countTaker-2"),
                    CountTaker.class);
    
    final int totalCount = MailboxSize * 2;
    
    for (int count = 1; count <= totalCount; ++count) {
      countTaker.take(count);
    }
    
    delay = 500L;
    pause();
    
    assertEquals(MaxCount, CountTakerActor.highest);
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    CountTakerActor.highest = 0;
    
    Properties properties = new Properties();
    properties.setProperty("plugin.name.testArrayQueueMailbox", "true");
    properties.setProperty("plugin.testArrayQueueMailbox.classname", "io.vlingo.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin");
    properties.setProperty("plugin.testArrayQueueMailbox.defaultMailbox", "false");
    properties.setProperty("plugin.testArrayQueueMailbox.size", ""+MailboxSize);
    properties.setProperty("plugin.testArrayQueueMailbox.fixedBackoff", "2");
    properties.setProperty("plugin.testArrayQueueMailbox.numberOfDispatchersFactor", "1.0");
    properties.setProperty("plugin.testArrayQueueMailbox.dispatcherThrottlingCount", "10");
    properties.setProperty("plugin.testArrayQueueMailbox.sendRetires", "10");
    
    PluginProperties pluginProps = new PluginProperties("testRingMailbox", properties);
    
    ManyToOneConcurrentArrayQueuePlugin provider = new ManyToOneConcurrentArrayQueuePlugin();
    
    provider.start(world, "testRingMailbox", pluginProps);
  }
  
  public static interface CountTaker {
    void take(final int count);
  }
  
  public static class CountTakerActor extends Actor implements CountTaker {
    public static int highest;
    private CountTaker self;
    
    public CountTakerActor() {
      self = selfAs(CountTaker.class);
    }
    
    @Override
    public void take(final int count) {
      if (count > highest) {
        highest = count;
      }
      if (count < MaxCount) {
        self.take(count + 1);
      }
    }
  }
}
