// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorsTest;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.plugin.PluginProperties;
import io.vlingo.xoom.actors.plugin.completes.PooledCompletesPlugin;
import io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueueMailbox;
import io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin;
import io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailbox;
import io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Properties;

public class MailboxTypeTest extends ActorsTest {

  @Test
  public void manyToOneQueueMailboxTest() {
    Properties properties = new Properties();
    properties.setProperty("plugin.name.testArrayQueueMailbox", "true");
    properties.setProperty("plugin.testArrayQueueMailbox.classname", "io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin");
    properties.setProperty("plugin.testArrayQueueMailbox.defaultMailbox", "false");

    ManyToOneConcurrentArrayQueuePlugin mailboxPlugin = new ManyToOneConcurrentArrayQueuePlugin();
    final PluginProperties pluginProperties = new PluginProperties("testArrayQueueMailbox", properties);
    final PooledCompletesPlugin completesPlugin = new PooledCompletesPlugin();

    completesPlugin.configuration().buildWith(world.configuration(), pluginProperties);
    mailboxPlugin.configuration().buildWith(world.configuration(), pluginProperties);

    mailboxPlugin.start(world);

    Empty empty = world.actorFor(Empty.class,
            Definition.has(EmptyActor.class, new ArrayList<>(), "testArrayQueueMailbox", "empty"));

    Assert.assertEquals(ManyToOneConcurrentArrayQueueMailbox.class, world.stage().mailboxTypeOf(empty));
    Assert.assertEquals("ManyToOneConcurrentArrayQueueMailbox", world.stage().mailboxTypeNameOf(empty));
  }

  @Test
  public void concurrentQueueMailboxTest() {
    Properties properties = new Properties();
    properties.setProperty("plugin.name.testConcurrentQueueMailbox", "true");
    properties.setProperty("plugin.testConcurrentQueueMailbox.classname", "io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin");
    properties.setProperty("plugin.testConcurrentQueueMailbox.defaultMailbox", "false");

    ConcurrentQueueMailboxPlugin mailboxPlugin = new ConcurrentQueueMailboxPlugin();
    final PluginProperties pluginProperties = new PluginProperties("testConcurrentQueueMailbox", properties);
    final PooledCompletesPlugin completesPlugin = new PooledCompletesPlugin();

    completesPlugin.configuration().buildWith(world.configuration(), pluginProperties);
    mailboxPlugin.configuration().buildWith(world.configuration(), pluginProperties);

    mailboxPlugin.start(world);

    Empty empty = world.actorFor(Empty.class,
            Definition.has(EmptyActor.class, new ArrayList<>(), "testConcurrentQueueMailbox", "empty"));

    Assert.assertEquals(ConcurrentQueueMailbox.class, world.stage().mailboxTypeOf(empty));
    Assert.assertEquals("ConcurrentQueueMailbox", world.stage().mailboxTypeNameOf(empty));
  }

  public interface Empty {
  }

  public static class EmptyActor extends Actor implements Empty {
  }
}
