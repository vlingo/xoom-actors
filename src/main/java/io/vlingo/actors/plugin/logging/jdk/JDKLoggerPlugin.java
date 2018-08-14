// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.jdk;

import java.util.Properties;
import java.util.logging.Handler;

import io.vlingo.actors.Configuration;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Logger;
import io.vlingo.actors.LoggerProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.AbstractPlugin;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginConfiguration;
import io.vlingo.actors.plugin.PluginProperties;

public class JDKLoggerPlugin extends AbstractPlugin implements Plugin, LoggerProvider {
  private final JDKLoggerPluginConfiguration jdkLoggerPluginConfiguration;
  private Logger logger;
  private int pass = 1;

  public static LoggerProvider registerStandardLogger(final String name, final Registrar registrar) {
    final JDKLoggerPlugin plugin = new JDKLoggerPlugin();
    final JDKLoggerPluginConfiguration pluginConfiguration = (JDKLoggerPluginConfiguration) plugin.configuration();
    final Properties properties = new Properties();
    properties.setProperty("plugin." + name + ".defaulLogger", "true");
    properties.setProperty("plugin." + name + ".handler.classname", "io.vlingo.actors.plugin.logging.jdk.DefaultHandler");
    properties.setProperty("plugin." + name + ".handler.name", name);
    properties.setProperty("plugin." + name + ".defaultLogger", "true");
    properties.setProperty("plugin." + name + ".handler.level", "ALL");
    pluginConfiguration.buildWith(registrar.world().configuration(), new PluginProperties(name, properties));
    plugin.start(registrar);
    return plugin;
  }

  public JDKLoggerPlugin() {
    this.jdkLoggerPluginConfiguration = JDKLoggerPluginConfiguration.define();
  }

  @Override
  public void close() {
    logger.close();
  }

  @Override
  public String name() {
    return jdkLoggerPluginConfiguration.name();
  }

  @Override
  public int pass() {
    return pass;
  }

  @Override
  public PluginConfiguration configuration() {
    return jdkLoggerPluginConfiguration;
  }

  @Override
  public void start(final Registrar registrar) {
    // pass 0 or 1 is bootstrap, pass 2 is for reals
    if (pass < 2) {
      logger = new JDKLogger(jdkLoggerPluginConfiguration.name(), jdkLoggerPluginConfiguration);
      registrar.register(jdkLoggerPluginConfiguration.name(), jdkLoggerPluginConfiguration.isDefaultLogger(), this);
      pass = 2;
    } else if (pass == 2 && registrar.world() != null) { // if this is a test there may not be a World
      logger = registrar.world().actorFor(Definition.has(JDKLoggerActor.class, Definition.parameters(logger), logger), Logger.class);
      registrar.register(jdkLoggerPluginConfiguration.name(), jdkLoggerPluginConfiguration.isDefaultLogger(), this);
    }
  }

  @Override
  public Logger logger() {
    return logger;
  }

  public static class JDKLoggerPluginConfiguration implements PluginConfiguration {
    private boolean defaultLogger;
    private boolean fileHandlerAppend = false;
    private int fileHandlerCount = 3;
    private String fileHandlerPattern = "vlingo-actors-log";
    private int fileHandlerLimit = 100_000_000;
    private Class<? extends Handler> handlerClass;
    private String handlerLevel;
    private String handlerName;
    private String memoryHandlerPushLevel;
    private int memoryHandlerSize = -1;
    private String memoryHandlerTarget;
    private String name = "jdkLogger";

    public static JDKLoggerPluginConfiguration define() {
      return new JDKLoggerPluginConfiguration();
    }

    public JDKLoggerPluginConfiguration defaultLogger() {
      this.defaultLogger = true;
      return this;
    }

    public boolean isDefaultLogger() {
      return defaultLogger;
    }

    public JDKLoggerPluginConfiguration fileHandlerAppend(final boolean fileHandlerAppend) {
      this.fileHandlerAppend = fileHandlerAppend;
      return this;
    }

    public boolean fileHandlerAppend() {
      return fileHandlerAppend;
    }

    public JDKLoggerPluginConfiguration fileHandlerCount(final int fileHandlerCount) {
      this.fileHandlerCount = fileHandlerCount;
      return this;
    }

    public int fileHandlerCount() {
      return fileHandlerCount;
    }

    public JDKLoggerPluginConfiguration fileHandlerLimit(final int fileHandlerLimit) {
      this.fileHandlerLimit = fileHandlerLimit;
      return this;
    }

    public int fileHandlerLimit() {
      return fileHandlerLimit;
    }

    public JDKLoggerPluginConfiguration fileHandlerPattern(final String fileHandlerPattern) {
      this.fileHandlerPattern = fileHandlerPattern;
      return this;
    }

    public String fileHandlerPattern() {
      return fileHandlerPattern;
    }

    public JDKLoggerPluginConfiguration handlerClass(final Class<? extends Handler> handlerClassname) {
      this.handlerClass = handlerClassname;
      return this;
    }

    public Class<? extends Handler> handlerClass() {
      return handlerClass;
    }

    public JDKLoggerPluginConfiguration handlerLevel(final String handlerLevel) {
      this.handlerLevel = handlerLevel;
      return this;
    }

    public String handlerLevel() {
      return handlerLevel;
    }

    public JDKLoggerPluginConfiguration handlerName(final String handlerName) {
      this.handlerName = handlerName;
      return this;
    }

    public String handlerName() {
      return handlerName;
    }

    public JDKLoggerPluginConfiguration memoryHandlerPushLevel(final String memoryHandlerPushLevel) {
      this.memoryHandlerPushLevel = memoryHandlerPushLevel;
      return this;
    }

    public String memoryHandlerPushLevel() {
      return memoryHandlerPushLevel;
    }

    public JDKLoggerPluginConfiguration memoryHandlerSize(final int memoryHandlerSize) {
      this.memoryHandlerSize = memoryHandlerSize;
      return this;
    }

    public int memoryHandlerSize() {
      return memoryHandlerSize;
    }

    public JDKLoggerPluginConfiguration memoryHandlerTarget(final String memoryHandlerTarget) {
      this.memoryHandlerTarget = memoryHandlerTarget;
      return this;
    }

    public String memoryHandlerTarget() {
      return memoryHandlerTarget;
    }

    public JDKLoggerPluginConfiguration name(final String name) {
      this.name = name;
      return this;
    }

    @Override
    public void build(final Configuration configuration) {
      configuration.with(
              defaultLogger()
             .name("vlingo/actors(test)")
             .handlerClass(DefaultHandler.class)
             .handlerName("vlingo")
             .handlerLevel("ALL"));
    }

    @Override
    public void buildWith(final Configuration configuration, final PluginProperties properties) {
      this.name = properties.name;

      this.defaultLogger = properties.getBoolean("defaultLogger", true);
      this.fileHandlerAppend = properties.getBoolean("filehandler.append", false);
      this.fileHandlerCount = properties.getInteger("filehandler.count", 3);
      this.fileHandlerPattern = properties.getString("filehandler.pattern", "vlingo-actors-log");
      this.fileHandlerLimit = properties.getInteger("filehandler.limit", 100_000_000);
      this.handlerClass = namedClassOr(properties.getString("handler.classname", null), DefaultHandler.class);
      this.handlerLevel = properties.getString("handler.level", "ALL");
      this.handlerName = properties.getString("handler.name", "ALL");
      this.memoryHandlerPushLevel = properties.getString("memoryhandler.pushLevel", null);
      this.memoryHandlerSize = properties.getInteger("memoryhandler.size", -1);
      this.memoryHandlerTarget = properties.getString("memoryhandler.target", null);
    }

    @Override
    public String name() {
      return name;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Handler> namedClassOr(final String handlerClassname, final Class<? extends Handler> handlerClass) {
      if (handlerClassname != null) {
        try {
          return (Class<? extends Handler>) Class.forName(handlerClassname);
        } catch (Exception e) {
          throw new IllegalArgumentException("Cannot load class: " + handlerClassname);
        }
      }
      return handlerClass;
    }
  }
}
