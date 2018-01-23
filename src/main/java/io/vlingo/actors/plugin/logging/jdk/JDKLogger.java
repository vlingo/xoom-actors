// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.jdk;

import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.MemoryHandler;

import io.vlingo.actors.Logger;
import io.vlingo.actors.plugin.PluginProperties;

public class JDKLogger implements Logger {
  private final Handler handler;
  private final Level level;
  private final java.util.logging.Logger logger;
  private final String name;

  public static Logger testInstance() {
    final Properties properties = new Properties();
    properties.setProperty("plugin.jdkLogger.handler.classname", "");
    
    final String name = "vlingo-test";
    
    final PluginProperties plugInProperties = new PluginProperties(name, properties);
    
    return new JDKLogger(name, plugInProperties);
  }
  
  protected JDKLogger(final String name, final PluginProperties properties) {
    this.name = name;
    this.logger = java.util.logging.Logger.getLogger(name);
    this.level = determineLevel(properties);
    this.logger.setLevel(this.level);
    this.handler = determineHandler(properties);
    this.logger.addHandler(handler);
  }

  @Override
  public void close() {
    java.util.logging.Logger.getGlobal().removeHandler(handler);
    java.util.logging.Logger.getLogger(name).removeHandler(handler);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void log(final String message) {
    if (isEnabled()) {
      logger.log(this.level, message);
    }
  }

  @Override
  public void log(final String message, final Throwable throwable) {
    if (isEnabled()) {
      logger.log(this.level, message, throwable);
    }
  }

  @Override
  public String name() {
    return name;
  }

  private Handler determineHandler(final PluginProperties properties) {
    final String classname = properties.getString("handler.classname", "java.util.logging.ConsoleHandler");

    try {
      if (classname.equals("java.util.logging.FileHandler")) {
        return loadFileHandler(properties);
      } else if (classname.equals("java.util.logging.MemoryHandler")) {
        return loadMemoryHandler(properties);
      } else {
        return loadNamedHandler(classname);
      }
      
    } catch (Exception e) {
      final Handler handler = new DefaultHandler();
      this.logger.addHandler(handler);
      this.logger.log(this.level, "vlingo/actors: Could not load the logger " + classname + " because: " + e.getMessage(), e);
      this.logger.log(this.level, "vlingo/actors: Instead we defaulted to: " + handler.getClass().getName());
      return handler;
    }
  }

  private Level determineLevel(final PluginProperties properties) {
    final String levelName = properties.getString("handler.level", "ALL");
    return java.util.logging.Level.parse(levelName);
  }
  
  private Handler loadFileHandler(final PluginProperties properties) throws Exception {
    final String pattern = properties.getString("filehandler.pattern", "vlingo-actors-log");
    final int limit = properties.getInteger("filehandler.limit", 100_000_000);
    final int count = properties.getInteger("filehandler.count", 3);
    final boolean append = properties.getBoolean("filehandler.append", false);
    
    return new FileHandler(pattern, limit, count, append);
  }

  private Handler loadMemoryHandler(final PluginProperties properties) throws Exception {
    final String target = properties.getString("memoryhandler.target", null);
    final int size = properties.getInteger("memoryhandler.size", -1);
    final String level = properties.getString("memoryhandler.pushLevel", null);
    
    final String message = "Must correctly configure target, size, and pushLevel for logging MemoryHandler.";

    if (target == null || size == -1 || level == null) {
      throw new IllegalArgumentException(message);
    }
    
    try {
      Class<?> targetClass = Class.forName(target);
      return new MemoryHandler((Handler) targetClass.newInstance(), size, Level.parse(level));
    } catch (Exception e) {
      throw new IllegalArgumentException(message, e);
    }
  }

  @SuppressWarnings("unchecked")
  private Handler loadNamedHandler(final String classname) throws Exception {
    Class<Handler> handlerClass = (Class<Handler>) Class.forName(classname);
    return handlerClass.newInstance();
  }
}
