// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.sharedringbuffer;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Definition;
import io.vlingo.actors.plugin.PluginProperties;
import io.vlingo.actors.plugin.completes.PooledCompletesPlugin;
import io.vlingo.actors.testkit.TestUntil;

public class RingBufferMailboxActorTest extends ActorsTest {
  private static final int MailboxSize = 64;
  private static final int MaxCount = 1024;
  
  private static final int ThroughputMailboxSize = 1048576;
  private static final int ThroughputMaxCount = 4194304; // 104857600;
  private static final int ThroughputWarmUpCount = 4194304;

  @Test
  public void testBasicDispatch() throws Exception {
    init(MailboxSize);

    final TestResults testResults = new TestResults();
    
    final CountTaker countTaker =
            world.actorFor(
                    Definition.has(CountTakerActor.class, Definition.parameters(testResults), "testRingMailbox", "countTaker-1"),
                    CountTaker.class);
    
    final int totalCount = MailboxSize / 2;
    
    testResults.until = until(MaxCount);
    testResults.maximum = MaxCount;
    
    for (int count = 1; count <= totalCount; ++count) {
      countTaker.take(count);
    }
    
    testResults.until.completes();
    
    assertEquals(MaxCount, testResults.highest);
  }

  @Test
  public void testThroughput() throws Exception {
    System.out.println("WARMING UP...");

    init(ThroughputMailboxSize);

    final TestResults testResults = new TestResults();

    final CountTaker countTaker =
            world.actorFor(
                    Definition.has(ThroughputCountTakerActor.class, Definition.parameters(testResults), "testRingMailbox", "countTaker-2"),
                    CountTaker.class);

    testResults.maximum = ThroughputWarmUpCount;

    for (int count = 1; count <= ThroughputWarmUpCount; ++count) {
      countTaker.take(count);
    }

    while (testResults.highest < ThroughputWarmUpCount)
      ;

    System.out.println("STARTING TEST...");

    testResults.highest = 0;
    testResults.maximum = ThroughputMaxCount;

    final long startTime = System.currentTimeMillis();

    for (int count = 1; count <= ThroughputMaxCount; ++count) {
      countTaker.take(count);
    }

    while (testResults.highest < ThroughputMaxCount)
      ;

    final long ticks = System.currentTimeMillis() - startTime;

    System.out.println("TICKS: " + ticks + " FOR " + ThroughputMaxCount + " MESSAGES IS " + (ThroughputMaxCount / ticks * 1000) + " PER SECOND");

    assertEquals(ThroughputMaxCount, testResults.highest);
  }

  private void init(final int mailboxSize) {
    Properties properties = new Properties();
    properties.setProperty("plugin.name.testRingMailbox", "true");
    properties.setProperty("plugin.testRingMailbox.classname", "io.vlingo.actors.plugin.mailbox.sharedringbuffer.SharedRingBufferMailboxPlugin");
    properties.setProperty("plugin.testRingMailbox.defaultMailbox", "false");
    properties.setProperty("plugin.testRingMailbox.size", ""+mailboxSize);
    properties.setProperty("plugin.testRingMailbox.fixedBackoff", "2");
    properties.setProperty("plugin.testRingMailbox.numberOfDispatchersFactor", "1.0");
    properties.setProperty("plugin.testRingMailbox.dispatcherThrottlingCount", "20");
    
    SharedRingBufferMailboxPlugin provider = new SharedRingBufferMailboxPlugin();
    final PluginProperties pluginProperties = new PluginProperties("testRingMailbox", properties);
    final PooledCompletesPlugin plugin = new PooledCompletesPlugin();
    plugin.configuration().buildWith(world.configuration(), pluginProperties);

    provider.start(world);
  }
  
  public static interface CountTaker {
    void take(final int count);
  }
  
  public static class CountTakerActor extends Actor implements CountTaker {
    private CountTaker self;
    private final TestResults testResults;
    
    public CountTakerActor(final TestResults testResults) {
      this.testResults = testResults;
      self = selfAs(CountTaker.class);
    }
    
    @Override
    public void take(final int count) {
      if (count > testResults.highest) {
        testResults.highest = count;
        testResults.until.happened();
      }
      if (count < testResults.maximum) {
        self.take(count + 1);
      } else {
        testResults.until.completeNow();
      }
    }
  }
  
  public static class ThroughputCountTakerActor extends Actor implements CountTaker {
    private final TestResults testResults;
    
    public ThroughputCountTakerActor(final TestResults testResults) {
      this.testResults = testResults;
    }
    
    @Override
    public void take(final int count) {
      testResults.highest = count;
    }
  }

  private static class TestResults {
    public volatile int highest = 0;
    public int maximum = 0;
    public TestUntil until = TestUntil.happenings(0);
  }
}
