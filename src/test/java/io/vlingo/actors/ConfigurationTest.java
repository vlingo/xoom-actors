// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vlingo.actors.plugin.completes.PooledCompletesPlugin.PooledCompletesPluginConfiguration;
import io.vlingo.actors.plugin.logging.jdk.DefaultHandler;
import io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin.JDKLoggerPluginConfiguration;
import io.vlingo.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin.ManyToOneConcurrentArrayQueuePluginConfiguration;
import io.vlingo.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin.ConcurrentQueueMailboxPluginConfiguration;
import io.vlingo.actors.plugin.mailbox.sharedringbuffer.SharedRingBufferMailboxPlugin.SharedRingBufferMailboxPluginConfiguration;
import io.vlingo.actors.plugin.supervision.CommonSupervisorsPlugin.CommonSupervisorsPluginConfiguration;
import io.vlingo.actors.plugin.supervision.DefaultSupervisorOverride;
import io.vlingo.actors.plugin.supervision.DefaultSupervisorOverridePlugin.DefaultSupervisorOverridePluginConfiguration;
import io.vlingo.actors.supervision.Ping;
import io.vlingo.actors.supervision.PingSupervisorActor;
import io.vlingo.actors.supervision.Pong;
import io.vlingo.actors.supervision.PongSupervisorActor;

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
                      .dispatcherThrottlingCount(10))
              .with(ManyToOneConcurrentArrayQueuePluginConfiguration
                      .define()
                      .ringSize(65535)
                      .fixedBackoff(2)
                      .dispatcherThrottlingCount(10)
                      .sendRetires(10))
              .with(ConcurrentQueueMailboxPluginConfiguration
                      .define()
                      .defaultMailbox()
                      .numberOfDispatchersFactor(1.5f)
                      .dispatcherThrottlingCount(10))
              .with(JDKLoggerPluginConfiguration
                      .define()
                      .defaultLogger()
                      .name("vlingo/actors(test)")
                      .handlerClass(DefaultHandler.class)
                      .handlerName("vlingo")
                      .handlerLevel("ALL"))
              .with(CommonSupervisorsPluginConfiguration
                      .define()
                      .supervisor("default", "pingSupervisor", Ping.class, PingSupervisorActor.class)
                      .supervisor("default", "pongSupervisor", Pong.class, PongSupervisorActor.class))
              .with(DefaultSupervisorOverridePluginConfiguration
                      .define()
                      .supervisor("default", "overrideSupervisor", DefaultSupervisorOverride.class))
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
    assertEquals(10, configuration.sharedRingBufferMailboxPluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.manyToOneConcurrentArrayQueuePluginConfiguration());
    assertFalse(configuration.manyToOneConcurrentArrayQueuePluginConfiguration().isDefaultMailbox());
    assertEquals(65535, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().ringSize());
    assertEquals(2, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().fixedBackoff());
    assertEquals(10, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.concurrentQueueMailboxPluginConfiguration());
    assertTrue(configuration.concurrentQueueMailboxPluginConfiguration().isDefaultMailbox());
    assertEquals(1.5f, configuration.concurrentQueueMailboxPluginConfiguration().numberOfDispatchersFactor(), 0);
    assertEquals(10, configuration.concurrentQueueMailboxPluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.jdkLoggerPluginConfiguration());
    assertTrue(configuration.jdkLoggerPluginConfiguration().isDefaultLogger());
    assertEquals("vlingo/actors(test)", configuration.jdkLoggerPluginConfiguration().name());
    assertEquals(DefaultHandler.class, configuration.jdkLoggerPluginConfiguration().handlerClass());
    assertEquals("vlingo", configuration.jdkLoggerPluginConfiguration().handlerName());
    assertEquals("ALL", configuration.jdkLoggerPluginConfiguration().handlerLevel());

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
    assertEquals(10, configuration.sharedRingBufferMailboxPluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.manyToOneConcurrentArrayQueuePluginConfiguration());
    assertFalse(configuration.manyToOneConcurrentArrayQueuePluginConfiguration().isDefaultMailbox());
    assertEquals(65535, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().ringSize());
    assertEquals(2, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().fixedBackoff());
    assertEquals(1, configuration.manyToOneConcurrentArrayQueuePluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.concurrentQueueMailboxPluginConfiguration());
    assertTrue(configuration.concurrentQueueMailboxPluginConfiguration().isDefaultMailbox());
    assertEquals(1.5f, configuration.concurrentQueueMailboxPluginConfiguration().numberOfDispatchersFactor(), 0);
    assertEquals(1, configuration.concurrentQueueMailboxPluginConfiguration().dispatcherThrottlingCount());

    assertNotNull(configuration.jdkLoggerPluginConfiguration());
    assertTrue(configuration.jdkLoggerPluginConfiguration().isDefaultLogger());
    assertEquals("vlingo/actors", configuration.jdkLoggerPluginConfiguration().name());
    assertEquals(DefaultHandler.class, configuration.jdkLoggerPluginConfiguration().handlerClass());
    assertEquals("vlingo", configuration.jdkLoggerPluginConfiguration().handlerName());
    assertEquals("ALL", configuration.jdkLoggerPluginConfiguration().handlerLevel());

    assertNotNull(configuration.defaultSupervisorOverridePluginConfiguration());
    assertEquals(1, configuration.defaultSupervisorOverridePluginConfiguration().count());
    assertEquals("default", configuration.defaultSupervisorOverridePluginConfiguration().stageName(0));
    assertEquals("overrideSupervisor", configuration.defaultSupervisorOverridePluginConfiguration().name(0));
    assertEquals(DefaultSupervisorOverride.class, configuration.defaultSupervisorOverridePluginConfiguration().supervisorClass(0));

    assertEquals("target/classes/", configuration.mainProxyGeneratedClassesPath());
    assertEquals("target/generated-sources/", configuration.mainProxyGeneratedSourcesPath());
    assertEquals("target/test-classes/", configuration.testProxyGeneratedClassesPath());
    assertEquals("target/generated-test-sources/", configuration.testProxyGeneratedSourcesPath());
  }
}
