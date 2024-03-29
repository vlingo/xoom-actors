// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorsTest;
import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Addressable;
import io.vlingo.xoom.actors.Addressable__Proxy;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.TestAddressableActor;
import io.vlingo.xoom.actors.__InternalOnlyAccessor;
import io.vlingo.xoom.actors.plugin.PluginProperties;
import io.vlingo.xoom.actors.plugin.completes.PooledCompletesPlugin;
import io.vlingo.xoom.actors.testkit.AccessSafely;

public class ManyToOneConcurrentArrayQueueMailboxActorTest extends ActorsTest {
  private static final int MailboxSize = 64;
  private static final int MaxCount = 1024;

  @Test
  public void testThatMailboxesAreDifferent() {
    final Addressable addressable1 =
            world.actorFor(
                    Addressable.class,
                    Definition.has(TestAddressableActor.class, Definition.NoParameters, "testArrayQueueMailbox", "addressable-1"));

    final Address address1 = ((Addressable__Proxy) addressable1).address();
    Actor actor1 = __InternalOnlyAccessor.actorOf(world.stage(), address1);
    Mailbox mailbox1 = __InternalOnlyAccessor.actorMailbox(actor1);
    
    final Addressable addressable2 =
            world.actorFor(
                    Addressable.class,
                    Definition.has(TestAddressableActor.class, Definition.NoParameters, "testArrayQueueMailbox", "addressable-1"));

    final Address address2 = ((Addressable__Proxy) addressable2).address();
    Actor actor2 = __InternalOnlyAccessor.actorOf(world.stage(), address2);
    Mailbox mailbox2 = __InternalOnlyAccessor.actorMailbox(actor2);

    assertNotEquals(mailbox1, mailbox2);
  }

  @Test
  public void testBasicDispatch() {
    final TestResults testResults = new TestResults(MaxCount);

    final CountTaker countTaker =
            world.actorFor(
                    CountTaker.class,
                    Definition.has(CountTakerActor.class, Definition.parameters(testResults), "testArrayQueueMailbox", "countTaker-1"));

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
                    Definition.has(CountTakerActor.class,
                            Definition.parameters(testResults),
                            "testArrayQueueMailbox",
                            "countTaker-2"));

     assertThrows(IllegalStateException.class, () -> {
       for (int count = 1; count <= MailboxSize + 1; ++count) {
         countTaker.take(count);
       }
     });
  }

  @Test
  public void testMailboxIsConfigured() {
    final TestResults testResults = new TestResults(MaxCount);
    CountTaker countTaker =
            world.actorFor(
                    CountTaker.class,
                    Definition.has(CountTakerActor.class,
                            Definition.parameters(testResults),
                            "testArrayQueueMailbox",
                            "countTaker"));

    String setMailboxTypeName = world.stage().mailboxTypeNameOf(countTaker);
    assertEquals("ManyToOneConcurrentArrayQueueMailbox", setMailboxTypeName);
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
