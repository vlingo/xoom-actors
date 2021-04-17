// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vlingo.xoom.actors.plugin.completes.PooledCompletesPlugin.PooledCompletesPluginConfiguration;
import io.vlingo.xoom.actors.plugin.logging.slf4j.Slf4jLoggerPlugin;
import io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin.ManyToOneConcurrentArrayQueuePluginConfiguration;
import io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin.ConcurrentQueueMailboxPluginConfiguration;
import io.vlingo.xoom.actors.plugin.mailbox.sharedringbuffer.SharedRingBufferMailboxPlugin.SharedRingBufferMailboxPluginConfiguration;
import io.vlingo.xoom.actors.plugin.supervision.CommonSupervisorsPlugin.CommonSupervisorsPluginConfiguration;
import io.vlingo.xoom.actors.plugin.supervision.DefaultSupervisorOverride;
import io.vlingo.xoom.actors.plugin.supervision.DefaultSupervisorOverridePlugin.DefaultSupervisorOverridePluginConfiguration;
import io.vlingo.xoom.actors.supervision.Ping;
import io.vlingo.xoom.actors.supervision.PingSupervisorActor;
import io.vlingo.xoom.actors.supervision.Pong;
import io.vlingo.xoom.actors.supervision.PongSupervisorActor;

public class ConfigurationTest {

  @Test
  public void testThatConfigurationConfirgures() {
    final Configuration configuration =
            Configuration
              .define()
              .with(PooledCompletesPluginConfiguration
                      .define()
                      .mailbox("queueMailbox")
                      .poolSize(10))
              .with(SharedRingBufferMailboxPluginConfiguration
                      .define()
                      .ringSize(65535)
                      .fixedBackoff(2)
                      .notifyOnSend(true)
                      .dispatcherThrottlingCount(10))
              .with(ManyToOneConcurrentArrayQueuePluginConfiguration
                      .define()
                      .ringSize(65535)
                      .fixedBackoff(2)
                      .notifyOnSend(true)
                      .dispatcherThrottlingCount(10)
                      .sendRetires(10))
              .with(ConcurrentQueueMailboxPluginConfiguration
                      .define()
                      .defaultMailbox()
                      .numberOfDispatchersFactor(1.5f)
                      .numberOfDispatchers(0)
                      .dispatcherThrottlingCount(10))
              .with(Slf4jLoggerPlugin.Slf4jLoggerPluginConfiguration
                      .define()
                      .defaultLogger()
                      .name("XOOM(test)"))
              .with(CommonSupervisorsPluginConfiguration
                      .define()
                      .supervisor("default", "pingSupervisor", Ping.class, PingSupervisorActor.class)
                      .supervisor("default", "pongSupervisor", Pong.class, PongSupervisorActor.class))
              .with(DefaultSupervisorOverridePluginConfiguration
                      .define()
                      .supervisor("default", "overrideSupervisor", DefaultSupervisorOverride.class))
              .with(DirectoryEvictionConfiguration
                  .define()
                  .fillRatioHigh(0.75F)
                  .lruThresholdMillis(10000))
              .usingMainProxyGeneratedClassesPath("target/classes/")
              .usingMainProxyGeneratedSourcesPath("target/generated-sources/")
              .usingTestProxyGeneratedClassesPath("target/test-classes/")
              .usingTestProxyGeneratedSourcesPath("target/generated-test-sources/");

    assertNotNull(configuration);
    assertNotNull(configuration.pooledCompletesPluginConfiguration());
    assertEquals("queueMailbox", configuration.pooledCompletesPluginConfiguration().mailbox());
    assertEquals(10, configuration.pooledCompletesPluginConfiguration().poolSize());

    assertNotNull(configuration.sharedRingBufferMailboxPluginConfiguration());
    assertFalse(configuration.sharedRingBufferMailboxPluginConfiguration().isDefaultMailbox());
    assertEquals(65535, configuration.sharedRingBufferMailboxPluginConfiguration().ringSize());
    assertEquals(2, configuration.sharedRingBufferMailboxPluginConfiguration().fixedBackoff());
    assertEquals(true, configuration.sharedRingBufferMailboxPluginConfiguration().notifyOnSend());
    assertEquals(10, configuration.sharedRingBufferMailboxPluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.manyToOneConcurrentArrayQueuePluginConfiguration());
    assertFalse(configuration.manyToOneConcurrentArrayQueuePluginConfiguration().isDefaultMailbox());
    assertEquals(65535, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().ringSize());
    assertEquals(2, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().fixedBackoff());
    assertEquals(true, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().notifyOnSend());
    assertEquals(10, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.concurrentQueueMailboxPluginConfiguration());
    assertTrue(configuration.concurrentQueueMailboxPluginConfiguration().isDefaultMailbox());
    assertEquals(1.5f, configuration.concurrentQueueMailboxPluginConfiguration().numberOfDispatchersFactor(), 0);
    assertEquals(10, configuration.concurrentQueueMailboxPluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.slf4jPluginConfiguration());
    assertTrue(configuration.slf4jPluginConfiguration().isDefaultLogger());
    assertEquals("XOOM(test)", configuration.slf4jPluginConfiguration().name());

    assertNotNull(configuration.commonSupervisorsPluginConfiguration());
    assertEquals(2, configuration.commonSupervisorsPluginConfiguration().count());
    assertEquals("default", configuration.commonSupervisorsPluginConfiguration().stageName(0));
    assertEquals("pingSupervisor", configuration.commonSupervisorsPluginConfiguration().name(0));
    assertEquals(Ping.class, configuration.commonSupervisorsPluginConfiguration().supervisedProtocol(0));
    assertEquals(PingSupervisorActor.class, configuration.commonSupervisorsPluginConfiguration().supervisorClass(0));
    assertEquals("default", configuration.commonSupervisorsPluginConfiguration().stageName(1));
    assertEquals("pongSupervisor", configuration.commonSupervisorsPluginConfiguration().name(1));
    assertEquals(Pong.class, configuration.commonSupervisorsPluginConfiguration().supervisedProtocol(1));
    assertEquals(PongSupervisorActor.class, configuration.commonSupervisorsPluginConfiguration().supervisorClass(1));

    assertNotNull(configuration.defaultSupervisorOverridePluginConfiguration());
    assertEquals(1, configuration.defaultSupervisorOverridePluginConfiguration().count());
    assertEquals("default", configuration.defaultSupervisorOverridePluginConfiguration().stageName(0));
    assertEquals("overrideSupervisor", configuration.defaultSupervisorOverridePluginConfiguration().name(0));
    assertEquals(DefaultSupervisorOverride.class, configuration.defaultSupervisorOverridePluginConfiguration().supervisorClass(0));

    assertNotNull(configuration.directoryEvictionConfiguration());
    assertEquals("directoryEviction", configuration.directoryEvictionConfiguration().name());
    assertFalse(configuration.directoryEvictionConfiguration().isEnabled());
    assertEquals(10000, configuration.directoryEvictionConfiguration().lruThresholdMillis());
    assertEquals(0.75F, configuration.directoryEvictionConfiguration().fillRatioHigh(), 0);

    assertEquals("target/classes/", configuration.mainProxyGeneratedClassesPath());
    assertEquals("target/generated-sources/", configuration.mainProxyGeneratedSourcesPath());
    assertEquals("target/test-classes/", configuration.testProxyGeneratedClassesPath());
    assertEquals("target/generated-test-sources/", configuration.testProxyGeneratedSourcesPath());
  }

  @Test
  public void testThatConfigurationDefaults() {
    final Configuration configuration = Configuration.define();
    configuration.load(0);

    assertNotNull(configuration);
    assertNotNull(configuration.pooledCompletesPluginConfiguration());
    assertEquals("queueMailbox", configuration.pooledCompletesPluginConfiguration().mailbox());
    assertEquals(10, configuration.pooledCompletesPluginConfiguration().poolSize());

    assertNotNull(configuration.sharedRingBufferMailboxPluginConfiguration());
    assertFalse(configuration.sharedRingBufferMailboxPluginConfiguration().isDefaultMailbox());
    assertEquals(65535, configuration.sharedRingBufferMailboxPluginConfiguration().ringSize());
    assertEquals(2, configuration.sharedRingBufferMailboxPluginConfiguration().fixedBackoff());
    assertEquals(false, configuration.sharedRingBufferMailboxPluginConfiguration().notifyOnSend());
    assertEquals(10, configuration.sharedRingBufferMailboxPluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.manyToOneConcurrentArrayQueuePluginConfiguration());
    assertFalse(configuration.manyToOneConcurrentArrayQueuePluginConfiguration().isDefaultMailbox());
    assertEquals(65535, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().ringSize());
    assertEquals(2, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().fixedBackoff());
    assertEquals(false, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().notifyOnSend());
    assertEquals(1, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.concurrentQueueMailboxPluginConfiguration());
    assertTrue(configuration.concurrentQueueMailboxPluginConfiguration().isDefaultMailbox());
    assertEquals(1.5f, configuration.concurrentQueueMailboxPluginConfiguration().numberOfDispatchersFactor(), 0);
    assertEquals(1, configuration.concurrentQueueMailboxPluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.slf4jPluginConfiguration());
    assertTrue(configuration.slf4jPluginConfiguration().isDefaultLogger());
    assertEquals("XOOM", configuration.slf4jPluginConfiguration().name());

    assertNotNull(configuration.defaultSupervisorOverridePluginConfiguration());
    assertEquals(1, configuration.defaultSupervisorOverridePluginConfiguration().count());
    assertEquals("default", configuration.defaultSupervisorOverridePluginConfiguration().stageName(0));
    assertEquals("overrideSupervisor", configuration.defaultSupervisorOverridePluginConfiguration().name(0));
    assertEquals(DefaultSupervisorOverride.class, configuration.defaultSupervisorOverridePluginConfiguration().supervisorClass(0));

    assertNotNull(configuration.directoryEvictionConfiguration());
    assertEquals("directoryEviction", configuration.directoryEvictionConfiguration().name());
    assertFalse(configuration.directoryEvictionConfiguration().isEnabled());
    assertEquals(600000, configuration.directoryEvictionConfiguration().lruThresholdMillis());
    assertEquals(0.8F, configuration.directoryEvictionConfiguration().fillRatioHigh(), 0);

    assertEquals("target/classes/", configuration.mainProxyGeneratedClassesPath());
    assertEquals("target/generated-sources/", configuration.mainProxyGeneratedSourcesPath());
    assertEquals("target/test-classes/", configuration.testProxyGeneratedClassesPath());
    assertEquals("target/generated-test-sources/", configuration.testProxyGeneratedSourcesPath());
  }

  @Test
  public void testThatConfigurationOverrides() {
    final float numberOfDispatchersFactor = 5.0f;

    final Configuration configuration =
            Configuration
              .define()
              .with(PooledCompletesPluginConfiguration
                      .define()
                      .mailbox("queueMailbox")
                      .poolSize(10))
              .with(ConcurrentQueueMailboxPluginConfiguration
                      .define()
                      .defaultMailbox()
                      .numberOfDispatchersFactor(numberOfDispatchersFactor)
                      .dispatcherThrottlingCount(10));

    assertEquals(numberOfDispatchersFactor, configuration.concurrentQueueMailboxPluginConfiguration().numberOfDispatchersFactor(), 0);

    final World world = World.start("override-config", configuration);

    final Mailbox mailbox = world.assignMailbox(world.findDefaultMailboxName(), 10);

    assertNotNull(mailbox);

    final int expected = (int) (Runtime.getRuntime().availableProcessors() * numberOfDispatchersFactor);
    assertEquals(expected, mailbox.concurrencyCapacity());
  }
}
