// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.MailboxConfiguration.ArrayQueueConfiguration;
import io.vlingo.xoom.actors.MailboxConfiguration.ConcurrentQueueConfiguration;
import io.vlingo.xoom.actors.MailboxConfiguration.SharedRingBufferConfiguration;
import io.vlingo.xoom.common.Completes;

public class MailboxConfigurationTest {
  private static final String MailboxName = "testConfigurationMailbox";
  private static final String PluginNamePerfix = "plugin.name.";
  private static final String PropertyNamePrefix = "plugin." + MailboxName;

  private World world;

  @Test
  public void testArrayQueueConfiguration() {
    final String classname = "io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueueMailboxPlugin";
    
    final ArrayQueueConfiguration arrayQueueConfiguration = MailboxConfiguration.arrayQueueConfiguration();
    
    assertNotNull(arrayQueueConfiguration);
    
    arrayQueueConfiguration.mailboxName(MailboxName);
    arrayQueueConfiguration.mailboxImplementationClassname(classname);
    arrayQueueConfiguration.defaultMailbox(true);
    arrayQueueConfiguration.size(33333);
    arrayQueueConfiguration.fixedBackoff(99);
    arrayQueueConfiguration.dispatcherThrottlingCount(7);
    arrayQueueConfiguration.notifyOnSend(true);
    arrayQueueConfiguration.sendRetires(14);
    
    final Properties properties = arrayQueueConfiguration.toProperties();
    
    assertEquals("true", properties.getProperty(PluginNamePerfix + MailboxName));
    assertEquals(classname, properties.getProperty(PropertyNamePrefix + ".classname"));
    assertEquals("true", properties.getProperty(PropertyNamePrefix + ".defaultMailbox"));
    assertEquals("33333", properties.getProperty(PropertyNamePrefix + ".size"));
    assertEquals("99", properties.getProperty(PropertyNamePrefix + ".fixedBackoff"));
    assertEquals("7", properties.getProperty(PropertyNamePrefix + ".dispatcherThrottlingCount"));
    assertEquals("true", properties.getProperty(PropertyNamePrefix + ".notifyOnSend"));
    assertEquals("14", properties.getProperty(PropertyNamePrefix + ".sendRetires"));

    world.registerMailboxType(arrayQueueConfiguration);
    
    final Greeter greeter =
            world
              .stage()
              .actorFor(
                      Greeter.class,
                      Definition.has(
                              GreeterActor.class,
                              Definition.NoParameters,
                              MailboxName,
                              "test-mailbox"));
    
    assertEquals("hello, world", greeter.hello("world").await());
  }

  @Test
  public void testConcurrentQueueConfiguration() {
    final String classname = "io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin";
    
    final ConcurrentQueueConfiguration concurrentQueueConfiguration = MailboxConfiguration.concurrentQueueConfiguration();
    
    assertNotNull(concurrentQueueConfiguration);
    
    concurrentQueueConfiguration.mailboxName(MailboxName);
    concurrentQueueConfiguration.mailboxImplementationClassname(classname);
    concurrentQueueConfiguration.defaultMailbox(true);
    concurrentQueueConfiguration.dispatcherThrottlingCount(7);
    concurrentQueueConfiguration.numberOfDispatchersFactor(2);
    concurrentQueueConfiguration.numberOfDispatchers(0);

    final Properties properties = concurrentQueueConfiguration.toProperties();
    
    assertEquals("true", properties.getProperty(PluginNamePerfix + MailboxName));
    assertEquals(classname, properties.getProperty(PropertyNamePrefix + ".classname"));
    assertEquals("true", properties.getProperty(PropertyNamePrefix + ".defaultMailbox"));
    assertEquals("2.0", properties.getProperty(PropertyNamePrefix + ".numberOfDispatchersFactor"));
    assertEquals("0", properties.getProperty(PropertyNamePrefix + ".numberOfDispatchers"));

    world.registerMailboxType(concurrentQueueConfiguration);
    
    final Greeter greeter =
            world
              .stage()
              .actorFor(
                      Greeter.class,
                      Definition.has(
                              GreeterActor.class,
                              Definition.NoParameters,
                              MailboxName,
                              "test-mailbox"));
    
    assertEquals("hello, world", greeter.hello("world").await());
  }

  @Test
  public void testSharedRingBufferConfiguration() {
    final String classname = "io.vlingo.xoom.actors.plugin.mailbox.sharedringbuffer.SharedRingBufferMailboxPlugin";
    
    final SharedRingBufferConfiguration sharedRingBufferConfiguration = MailboxConfiguration.sharedRingBufferConfiguration();
    
    assertNotNull(sharedRingBufferConfiguration);
    
    sharedRingBufferConfiguration.mailboxName(MailboxName);
    sharedRingBufferConfiguration.mailboxImplementationClassname(classname);
    sharedRingBufferConfiguration.defaultMailbox(true);
    sharedRingBufferConfiguration.size(33333);
    sharedRingBufferConfiguration.fixedBackoff(99);
    sharedRingBufferConfiguration.dispatcherThrottlingCount(7);
    sharedRingBufferConfiguration.notifyOnSend(true);
    
    final Properties properties = sharedRingBufferConfiguration.toProperties();
    
    assertEquals("true", properties.getProperty(PluginNamePerfix + MailboxName));
    assertEquals(classname, properties.getProperty(PropertyNamePrefix + ".classname"));
    assertEquals("true", properties.getProperty(PropertyNamePrefix + ".defaultMailbox"));
    assertEquals("33333", properties.getProperty(PropertyNamePrefix + ".size"));
    assertEquals("99", properties.getProperty(PropertyNamePrefix + ".fixedBackoff"));
    assertEquals("7", properties.getProperty(PropertyNamePrefix + ".dispatcherThrottlingCount"));
    assertEquals("true", properties.getProperty(PropertyNamePrefix + ".notifyOnSend"));

    world.registerMailboxType(sharedRingBufferConfiguration);

    final Greeter greeter =
            world
              .stage()
              .actorFor(
                      Greeter.class,
                      Definition.has(
                              GreeterActor.class,
                              Definition.NoParameters,
                              MailboxName,
                              "test-mailbox"));
    
    assertEquals("hello, world", greeter.hello("world").await());
  }
  
  @Before
  public void setUp() {
    world = World.startWithDefaults("mailbox-configuration-test");
  }

  @After
  public void tearDown() {
    world.terminate();
  }

  public static interface Greeter {
    public static final String Hello = "hello, ";

    Completes<String> hello(final String to);
  }
  
  public static class GreeterActor extends Actor implements Greeter {
    public GreeterActor() {
      super();
    }

    @Override
    public Completes<String> hello(String to) {
      return completes().with(Hello + to);
    }
  }
}
