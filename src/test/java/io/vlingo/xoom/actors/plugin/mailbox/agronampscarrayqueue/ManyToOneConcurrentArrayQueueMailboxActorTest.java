// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue;

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.xoom.actors.Protocols;
import io.vlingo.xoom.actors.testkit.TestUntil;
import io.vlingo.xoom.common.Completes;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorsTest;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.plugin.PluginProperties;
import io.vlingo.xoom.actors.plugin.completes.PooledCompletesPlugin;
import io.vlingo.xoom.actors.testkit.AccessSafely;

public class ManyToOneConcurrentArrayQueueMailboxActorTest extends ActorsTest {
  private static final int MailboxSize = 64;
  private static final int MaxCount = 1024;

  @Test
  public void testBasicDispatch() {
    final TestResults testResults = new TestResults(MaxCount);

    final CountTaker countTaker =
            world.actorFor(
                    CountTaker.class,
                    Definition.has(CountTakerActor.class, Definition.parameters(testResults), "testRingMailbox", "countTaker-1"));

    final int totalCount = MailboxSize / 2;

    for (int count = 1; count <= totalCount; ++count) {
      countTaker.take(count);
    }

    assertEquals(MaxCount, testResults.getHighest());
  }

  @Test
  public void testOverflowDispatch() {
    final TestResults testResults = new TestResults(MaxCount);

    final CountTaker countTaker =
            world.actorFor(
                    CountTaker.class,
                    Definition.has(CountTakerActor.class, Definition.parameters(testResults), "testArrayQueueMailbox", "countTaker-2"));

    final int totalCount = MailboxSize * 2;

    for (int count = 1; count <= totalCount; ++count) {
      countTaker.take(count);
    }


    assertEquals(MaxCount, testResults.getHighest());
  }

  @Test
  public void testMessageQueue() {
    final TestResults testResults = new TestResults(1);

    CountTaker countTaker =
            world.actorFor(
                    CountTaker.class,
                    Definition.has(CountTakerActor.class,
                            Definition.parameters(testResults),
                            "testArrayQueueMailbox",
                            "countTaker-2"));

    countTaker.getMailboxClassName().andFinallyConsume(testResults::setMailboxName);

    assertEquals("ManyToOneConcurrentArrayQueueMailbox", testResults.getMailboxName());
  }

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();

    Properties properties = new Properties();
    properties.setProperty("plugin.name.testArrayQueueMailbox", "true");
    properties.setProperty("plugin.testArrayQueueMailbox.classname", "io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin");
    properties.setProperty("plugin.testArrayQueueMailbox.defaultMailbox", "false");
    properties.setProperty("plugin.testArrayQueueMailbox.size", ""+MailboxSize);
    properties.setProperty("plugin.testArrayQueueMailbox.fixedBackoff", "2");
    properties.setProperty("plugin.testArrayQueueMailbox.dispatcherThrottlingCount", "1");
    properties.setProperty("plugin.testArrayQueueMailbox.sendRetires", "10");


    ManyToOneConcurrentArrayQueuePlugin manyToOneConcurrentArrayQueuePlugin = new ManyToOneConcurrentArrayQueuePlugin();
    final PluginProperties pluginProperties = new PluginProperties("testArrayQueueMailbox", properties);
    final PooledCompletesPlugin pooledCompletesPlugin = new PooledCompletesPlugin();

    pooledCompletesPlugin.configuration().buildWith(world.configuration(), pluginProperties);
    pooledCompletesPlugin.start(world);

    manyToOneConcurrentArrayQueuePlugin.configuration().buildWith(world.configuration(), pluginProperties);
    manyToOneConcurrentArrayQueuePlugin.start(world);
  }

  public interface CountTaker {
    void take(final int count);
    Completes<String> getMailboxClassName();
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
      if (count < MaxCount) {
        self.take(count + 1);
      }
    }

    @Override
    public Completes<String> getMailboxClassName() {
      return completes().with(getMailboxClass().getSimpleName());
    }
  }

  private static class TestResults {
    private final AccessSafely accessSafely;

    private TestResults(final int happenings) {
      final AtomicInteger highest = new AtomicInteger(0);
      final AtomicReference<String> mailbox = new AtomicReference<>();

      this.accessSafely = AccessSafely
              .afterCompleting(happenings)
              .writingWith("name", mailbox::set)
              .readingWith("name", mailbox::get)
              .writingWith("highest", highest::set)
              .readingWith("highest", highest::get)
              .readingWith("isHighest", (Integer count) -> count > highest.get());
    }


    void setMailboxName(String name) {
      this.accessSafely.writeUsing("name", name);
    }

    String getMailboxName() {
      return this.accessSafely.readFrom("name");
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
