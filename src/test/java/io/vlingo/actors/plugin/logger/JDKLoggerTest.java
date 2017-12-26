// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logger;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.LoggerProvider;
import io.vlingo.actors.MailboxProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.PluginProperties;
import io.vlingo.actors.plugin.logging.Logger;
import io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin;

public class JDKLoggerTest {
  private Logger logger;
  
  @Test
  public void testLog() throws Exception {
    logger.log("TEST");
  }
  
  @Before
  public void setUp() {
    Properties properties = new Properties();
    properties.setProperty("plugin.name.jdkLogger", "true");
    properties.setProperty("plugin.jdkLogger.classname", "io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin");
    properties.setProperty("plugin.jdkLogger.defaulLogger", "true");
    
    final Registrar registrar = new Registrar() {
      @Override
      public void register(String name, boolean isDefault, MailboxProvider mailboxProvider) {
      }

      @Override
      public void register(String name, boolean isDefault, LoggerProvider loggerProvider) {
        System.out.println("REGISTERED LOGGER PROVIDER: " + loggerProvider.getClass().getName());
      }
    };
    
    JDKLoggerPlugin plugin = new JDKLoggerPlugin();
    plugin.start(registrar, "jdkLogger", new PluginProperties("jdkLogger", properties));
    
    logger = plugin.logger();
  }
}
