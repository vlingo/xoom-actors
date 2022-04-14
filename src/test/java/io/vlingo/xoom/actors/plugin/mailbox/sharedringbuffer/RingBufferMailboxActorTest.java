// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.sharedringbuffer;

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorsTest;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.plugin.PluginProperties;
import io.vlingo.xoom.actors.plugin.completes.PooledCompletesPlugin;
import io.vlingo.xoom.actors.testkit.AccessSafely;

public class RingBufferMailboxActorTest extends ActorsTest {
  private static final int MailboxSize = 64;
  private static final int MaxCount = 1024;

  private static final int ThroughputMailboxSize = 1048576;
  private static final int ThroughputMaxCount = 4194304; // 104857600;
  private static final int ThroughputWarmUpCount = 4194304;

  @Test
  public void testBasicDispatch() {
    init(MailboxSize);

    final TestResults testResults = new TestResults(MaxCount + 1);

    final CountTaker countTaker =
            world.actorFor(
                    CountTaker.class,
                    Definition.has(CountTakerActor.class, Definition.parameters(testResults), "testRingMailbox", "countTaker-1"));

    testResults.setMaximum(MaxCount);

    for (int count = 1; count <= MaxCount; ++count) {
      countTaker.take(count);
    }

    assertEquals(MaxCount, testResults.getHighest());
  }

  @Ignore
  @Test
  public void testThroughput() throws Exception {
    System.out.println("WARMING UP...");

    init(ThroughputMailboxSize);

    final TestResults testResults = new TestResults(ThroughputMailboxSize);

    final CountTaker countTaker =
            world.actorFor(
                    CountTaker.class,
                    Definition.has(ThroughputCountTakerActor.class, Definition.parameters(testResults), "testRingMailbox", "countTaker-2"));

    testResults.setMaximum(ThroughputWarmUpCount);

    for (int count = 1; count <= ThroughputWarmUpCount; ++count) {
      countTaker.take(count);
    }

    while (testResults.getHighest() < ThroughputWarmUpCount)
      ;

    System.out.println("STARTING TEST...");

    testResults.setHighest(0);
    testResults.setMaximum(ThroughputMaxCount);

    final long startTime = System.currentTimeMillis();

    for (int count = 1; count <= ThroughputMaxCount; ++count) {
      countTaker.take(count);
    }

    while (testResults.getHighest() < ThroughputMaxCount)
      ;

    final long ticks = System.currentTimeMillis() - startTime;

    System.out.println("TICKS: " + ticks + " FOR " + ThroughputMaxCount + " MESSAGES IS " + (ThroughputMaxCount / ticks * 1000) + " PER SECOND");

    assertEquals(ThroughputMaxCount, testResults.getHighest());
  }

  private void init(final int mailboxSize) {
    Properties properties = new Properties();
    properties.setProperty("plugin.name.testRingMailbox", "true");
    properties.setProperty("plugin.testRingMailbox.classname", "io.vlingo.xoom.actors.plugin.mailbox.sharedringbuffer.SharedRingBufferMailboxPlugin");
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
    @SuppressWarnings("unused")
    private CountTaker self;
    private final TestResults testResults;

    public CountTakerActor(final TestResults testResults) {
      this.testResults = testResults;
      self = selfAs(CountTaker.class);
    }

    @Override
    public void take(final int count) {
      if (testResults.isHighest(count)) {
        testResults.setHighest(count);
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
      testResults.setHighest(count);
    }
  }

  private static class TestResults {
    private final AccessSafely accessSafely;

    private TestResults(final int happenings) {
      final AtomicInteger highest = new AtomicInteger(0);
      final AtomicInteger maximum = new AtomicInteger(0);
      this.accessSafely = AccessSafely
              .afterCompleting(happenings)
              .writingWith("highest", highest::set)
              .readingWith("highest", highest::get)
              .writingWith("maximum", maximum::set)
              .readingWith("maximum", maximum::get)
              .readingWith("isHighest", (Integer count) -> count > highest.get());
    }

    void setHighest(Integer value) {
      this.accessSafely.writeUsing("highest", value);
    }
    void setMaximum(Integer value) {
      this.accessSafely.writeUsing("maximum", value);
    }

    int getHighest(){
      return this.accessSafely.readFrom("highest");
    }
    @SuppressWarnings("unused")
    int getMaximum(){
      return this.accessSafely.readFrom("maximum");
    }

    boolean isHighest(Integer value){
      return this.accessSafely.readFromNow("isHighest", value);
    }
  }

}
