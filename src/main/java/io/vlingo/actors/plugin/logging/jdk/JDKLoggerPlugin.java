// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.jdk;

import java.util.Properties;

import io.vlingo.actors.LoggerProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginProperties;
import io.vlingo.actors.plugin.logging.Logger;

public class JDKLoggerPlugin implements Plugin, LoggerProvider {
  private JDKLogger logger;
  private String name;
  
  public static LoggerProvider registerTestLogger(final Registrar registrar) {
    Properties properties = new Properties();
    properties.setProperty("plugin.jdkLogger.defaulLogger", "true");
    JDKLoggerPlugin plugin = new JDKLoggerPlugin();
    plugin.start(registrar, "jdkLogger", new PluginProperties("jdkLogger", properties));
    return plugin;
  }
  
  @Override
  public void close() { }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void start(final Registrar registrar, final String name, final PluginProperties properties) {
    this.name = name;
    this.logger = new JDKLogger(name, "vlingo");
    
    registrar.register(name, properties.getBoolean("defaulLogger", true), this);
  }

  @Override
  public Logger logger() {
    return logger;
  }
}
