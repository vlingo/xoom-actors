// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.slf4j;

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

public class Slf4jLoggerPluginTest {
  private boolean registered;

  private final Registrar registrar = new MockRegistrar() {
    @Override
    public void register(String name, boolean isDefault, LoggerProvider loggerProvider) {
      registered = true;
    }
  };

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