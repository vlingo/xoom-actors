// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import io.vlingo.actors.plugin.completes.MockRegistrar;
import org.junit.After;
import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.CompletesEventuallyProvider;
import io.vlingo.actors.Configuration;
import io.vlingo.actors.Logger;
import io.vlingo.actors.LoggerProvider;
import io.vlingo.actors.MailboxProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.World;
import io.vlingo.actors.plugin.PluginProperties;
import io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin;

public class JDKLoggerTest {
  private Logger logger;
  private boolean registered;
  private World world;
  
  final Registrar registrar = new MockRegistrar() {
    @Override
    public void register(String name, boolean isDefault, LoggerProvider loggerProvider) {
      registered = true;
    }
  };

  @Test
  public void testLoggedMessagesCount() throws Exception {
    Properties properties = new Properties();
    properties.setProperty("plugin.name.testLoggedMessagesCount", "true");
    properties.setProperty("plugin.testLoggedMessagesCount.classname", "io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin");
    properties.setProperty("plugin.testLoggedMessagesCount.defaultLogger", "true");
    properties.setProperty("plugin.testLoggedMessagesCount.handler.classname", "java.util.logging.MemoryHandler");
    properties.setProperty("plugin.testLoggedMessagesCount.handler.level", "ALL");
    properties.setProperty("plugin.testLoggedMessagesCount.memoryhandler.target", "io.vlingo.actors.plugin.logger.MockHandler");
    properties.setProperty("plugin.testLoggedMessagesCount.memoryhandler.size", "1024");
    properties.setProperty("plugin.testLoggedMessagesCount.memoryhandler.pushLevel", "ALL");
    
    final Configuration configuration = Configuration.define();
    final JDKLoggerPlugin plugin = new JDKLoggerPlugin();
    plugin.configuration().buildWith(configuration, new PluginProperties("testLoggedMessagesCount", properties));
    
    plugin.start(registrar);
    
    logger = plugin.logger();
    
    assertEquals("testLoggedMessagesCount", plugin.logger().name());

    logger.log("TEST:1 1");
    logger.log("TEST:1 2");
    logger.log("TEST:1 3");
    
    assertEquals(3, MockHandler.instance.get().logMessagesCount.get());
  }

  @Test
  public void testNamedHandler() throws Exception {
    Properties properties = new Properties();
    properties.setProperty("plugin.name.testNamedHandler", "true");
    properties.setProperty("plugin.testNamedHandler.classname", "io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin");
    properties.setProperty("plugin.testNamedHandler.defaultLogger", "true");
    properties.setProperty("plugin.testNamedHandler.handler.classname", "io.vlingo.actors.plugin.logger.MockHandler");
    properties.setProperty("plugin.testNamedHandler.handler.level", "ALL");
    
    final Configuration configuration = Configuration.define();
    final JDKLoggerPlugin plugin = new JDKLoggerPlugin();
    plugin.configuration().buildWith(configuration, new PluginProperties("testNamedHandler", properties));
    plugin.start(registrar);
    
    logger = plugin.logger();
    
    assertEquals("testNamedHandler", plugin.logger().name());

    logger.log("TEST:2 1");
    logger.log("TEST:2 2");
    logger.log("TEST:2 3");
    
    assertEquals(3, MockHandler.instance.get().logMessagesCount.get());
  }

  @Test
  public void testRegistration() throws Exception {
    registered = false;
    
    final Configuration configuration = Configuration.define();
    final JDKLoggerPlugin plugin = new JDKLoggerPlugin();
    plugin.configuration().buildWith(configuration, new PluginProperties("testRegistration", new Properties()));
    
    plugin.start(registrar);
    
    assertTrue(registered);
    
    // although unnamed, the default log handler type will be used: ConsoleHandler
    logger = plugin.logger();
    
    assertNotNull(logger);
    assertEquals("testRegistration", plugin.logger().name());
    
    logger.log("TEST:3 1");
    logger.log("TEST:3 2");
    logger.log("TEST:3 3");
  }
  
  @Test
  public void testStandardLogger() {
    world = World.start("test-standard-logger");
    
    logger = LoggerProvider.standardLoggerProvider(world, "testStandardLogger").logger();
    
    assertNotNull(logger);
    assertEquals("testStandardLogger", logger.name());
    
    logger.log("TEST:4 1");
    logger.log("TEST:4 2");
    logger.log("TEST:4 3");
  }
  
  @After
  public void tearDown() {
    logger.close();
    
    if (world != null) {
      world.terminate();
    }
  }
}
