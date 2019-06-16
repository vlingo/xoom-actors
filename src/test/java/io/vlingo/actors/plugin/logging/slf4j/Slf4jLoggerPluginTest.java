// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.slf4j;

import io.vlingo.actors.ActorsTest;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.vlingo.actors.Configuration;
import io.vlingo.actors.Logger;
import io.vlingo.actors.LoggerProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.PluginProperties;
import io.vlingo.actors.plugin.completes.MockRegistrar;

public class Slf4jLoggerPluginTest extends ActorsTest {
  private boolean registered;

  private final Registrar registrar = new MockRegistrar() {
    @Override
    public void register(String name, boolean isDefault, LoggerProvider loggerProvider) {
      registered = true;
    }
  };


  @Test
  public void testStandardLogger() {
    Logger logger = LoggerProvider.standardLoggerProvider(world, "testStandardLogger").logger();

    assertNotNull(logger);
    assertEquals("testStandardLogger", logger.name());

    logger.trace("TRACE message");
    logger.trace("TRACE message with parameters {0}", "1");
    logger.trace("TRACE message with exception", new Exception("test trace exception"));

    logger.debug("DEBUG message");
    logger.debug("DEBUG message with parameters {0}", "2");
    logger.debug("DEBUG message with exception", new Exception("test debug exception"));

    logger.info("INFO message");
    logger.info("INFO message with parameters {0}", "3");
    logger.info("INFO message with exception", new Exception("test info exception"));

    logger.warn("WARN message");
    logger.warn("WARN message with parameters {0}", "4");
    logger.warn("WARN message with exception", new Exception("test warn exception"));

    logger.error("ERROR message");
    logger.error("ERROR message with parameters {0}", "4");
    logger.error("ERROR message with exception", new Exception("test error exception"));
  }

  @Test
  public void testRegistration() {
    final Configuration configuration = Configuration.define();
    final Slf4jLoggerPlugin plugin = new Slf4jLoggerPlugin();
    plugin.configuration().buildWith(configuration, new PluginProperties("slf4jTestRegistration", new Properties()));

    plugin.start(registrar);

    assertTrue(registered);

    Logger logger = plugin.logger();

    assertNotNull(logger);
    assertEquals("slf4jTestRegistration", plugin.name());

    logger.debug("TEST:3 1");
    logger.debug("TEST:3 2");
    logger.debug("TEST:3 3");
  }

}