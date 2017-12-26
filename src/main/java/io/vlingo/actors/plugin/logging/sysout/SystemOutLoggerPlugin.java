// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.sysout;

import io.vlingo.actors.Logger;
import io.vlingo.actors.LoggerProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginProperties;

public class SystemOutLoggerPlugin implements Plugin, LoggerProvider {
  private final Logger logger;
  private String name;
  
  public SystemOutLoggerPlugin() {
    this.logger = new SystemOutLogger();
  }

  @Override
  public void close() { }

  @Override
  public Logger logger() {
    return logger;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void start(final Registrar registrar, final String name, final PluginProperties properties) {
    this.name = name;
    
    registrar.register(name, properties.getBoolean("defaulLogger", false), this);
  }
}
