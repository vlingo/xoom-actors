// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors.plugin.logging.slf4j;

import io.vlingo.actors.Configuration;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Logger;
import io.vlingo.actors.LoggerProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.AbstractPlugin;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginConfiguration;
import io.vlingo.actors.plugin.PluginProperties;

public class Slf4jLoggerPlugin extends AbstractPlugin implements Plugin, LoggerProvider {
  private final Slf4jLoggerPluginConfiguration pluginConfiguration;
  private int pass = 1;
  private Logger logger;

  /**
   * Required for plugin creation at runtime.
   */
  public Slf4jLoggerPlugin() {
    this.pluginConfiguration = new Slf4jLoggerPluginConfiguration();
  }

  private Slf4jLoggerPlugin(final PluginConfiguration configuration) {
    this.pluginConfiguration = ((Slf4jLoggerPluginConfiguration) configuration);
  }

  @Override
  public Logger logger() {
    return this.logger;
  }

  @Override
  public void close() {
    this.logger.close();
  }

  @Override
  public PluginConfiguration configuration() {
    return pluginConfiguration;
  }

  @Override
  public String name() {
    return pluginConfiguration.name();
  }

  @Override
  public int pass() {
    return pass;
  }

  @Override
  public void start(Registrar registrar) {
    // pass 0 or 1 is bootstrap, pass 2 is for reals
    if (pass < 2) {
      logger = new Slf4jLogger();
      registrar.register(this.pluginConfiguration.name(), this.pluginConfiguration.isDefaultLogger(), this);
      pass = 2;
    } else if (pass == 2 && registrar.world() != null) { // if this is a test there may not be a World
      logger = registrar.world()
              .actorFor(Logger.class, Definition.has(Slf4jLoggerActor.class, Definition.parameters(logger), logger));
      registrar.register(this.pluginConfiguration.name(), this.pluginConfiguration.isDefaultLogger(), this);
    }
  }

  @Override
  public Plugin with(PluginConfiguration overrideConfiguration) {
    if (overrideConfiguration == null) {
      return this;
    }
    return new Slf4jLoggerPlugin(overrideConfiguration);
  }

  public static class Slf4jLoggerPluginConfiguration implements PluginConfiguration {
    private boolean defaultLogger;
    private String name;

    public boolean isDefaultLogger() {
      return defaultLogger;
    }

    @Override
    public String name() {
      return this.name;
    }

    @Override
    public void build(Configuration configuration) {
      configuration.with(defaultLogger().name("vlingo/actors"));
    }

    @Override
    public void buildWith(Configuration configuration, PluginProperties properties) {
      this.name = properties.name;
      this.defaultLogger = properties.getBoolean("defaultLogger", true);
    }

    public Slf4jLoggerPluginConfiguration defaultLogger() {
      this.defaultLogger = true;
      return this;
    }

    public Slf4jLoggerPluginConfiguration name(final String name) {
      this.name = name;
      return this;
    }

    public static Slf4jLoggerPluginConfiguration define() {
      return new Slf4jLoggerPluginConfiguration();
    }
  }
}
