// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors.plugin.logging.slf4j;

import java.util.Properties;

import io.vlingo.actors.Configuration;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Logger;
import io.vlingo.actors.LoggerProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.AbstractPlugin;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginConfiguration;
import io.vlingo.actors.plugin.PluginProperties;
import io.vlingo.actors.plugin.logging.slf4j.Slf4jLoggerActor.Slf4jLoggerInstantiator;

public class Slf4jLoggerPlugin extends AbstractPlugin implements Plugin, LoggerProvider {
  private final Slf4jLoggerPluginConfiguration pluginConfiguration;
  private int pass = 1;
  private Logger logger;

  public static Logger basicInstance() {
    final Configuration configuration = Configuration.define();
    final Slf4jLoggerPlugin.Slf4jLoggerPluginConfiguration loggerConfiguration
            = Slf4jLoggerPlugin.Slf4jLoggerPluginConfiguration.define();
    loggerConfiguration.build(configuration);
    return new Slf4jLogger(loggerConfiguration.name());
  }

  public static LoggerProvider registerStandardLogger(final String name, final Registrar registrar) {
    final Slf4jLoggerPlugin plugin = new Slf4jLoggerPlugin();
    final Slf4jLoggerPluginConfiguration pluginConfiguration = (Slf4jLoggerPluginConfiguration) plugin.configuration();
    final Properties properties = new Properties();
    properties.setProperty("plugin." + name + ".defaulLogger", "true");
    pluginConfiguration.buildWith(registrar.world().configuration(), new PluginProperties(name, properties));
    plugin.start(registrar);
    return plugin;
  }

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
      logger = new Slf4jLogger(this.pluginConfiguration.name());
      registrar.register(this.pluginConfiguration.name(), this.pluginConfiguration.isDefaultLogger(), this);
      pass = 2;
    } else if (pass == 2 && registrar.world() != null) { // if this is a test there may not be a World
      logger = registrar.world()
              .actorFor(Logger.class, Definition.has(Slf4jLoggerActor.class, new Slf4jLoggerInstantiator(logger), logger));
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
