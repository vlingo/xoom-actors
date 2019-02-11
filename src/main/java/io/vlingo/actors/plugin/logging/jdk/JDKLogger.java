// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.jdk;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.MemoryHandler;

import io.vlingo.actors.Configuration;
import io.vlingo.actors.Logger;
import io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin.JDKLoggerPluginConfiguration;

public class JDKLogger implements Logger {
  private final Handler handler;
  private final Level level;
  private final java.util.logging.Logger logger;
  private final String name;

  public static Logger basicInstance() {
    final Configuration configuration = Configuration.define();
    final JDKLoggerPluginConfiguration loggerConfiguration = JDKLoggerPluginConfiguration.define();
    loggerConfiguration.build(configuration);
    return new JDKLogger(loggerConfiguration.name(), loggerConfiguration);
  }

  public static Logger testInstance() {
    return basicInstance();
  }

  protected JDKLogger(final String name, final JDKLoggerPluginConfiguration confirguration) {
    this.name = name;
    this.logger = java.util.logging.Logger.getLogger(name);
    this.level = java.util.logging.Level.parse(confirguration.handlerLevel());
    this.logger.setLevel(this.level);
    this.handler = determineHandler(confirguration);
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
      try {
        logger.log(this.level, message);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final PrintStream stream = new PrintStream(output);
        throwable.printStackTrace(stream);
        final String stacktrace = output.toString("UTF-8");
        logger.log(this.level, stacktrace);
      } catch (Exception e) {
        logger.severe("JDKLogger: Failed to log exception about: " + message + " and reason: " + throwable.getMessage());
      }
    }
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " TYPE: " + handler;
  }

  private Handler determineHandler(final JDKLoggerPluginConfiguration confirguration) {
    final String classname = confirguration.handlerClass().getName();
    try {
      if (classname.equals("java.util.logging.FileHandler")) {
        return loadFileHandler(confirguration);
      } else if (classname.equals("java.util.logging.MemoryHandler")) {
        return loadMemoryHandler(confirguration);
      } else {
        return loadNamedHandler(confirguration);
      }
      
    } catch (Exception e) {
      final Handler handler = new DefaultHandler();
      this.logger.addHandler(handler);
      this.logger.log(this.level, "vlingo/actors: Could not load the logger " + confirguration.name() + " because: " + e.getMessage(), e);
      this.logger.log(this.level, "vlingo/actors: Instead we defaulted to: " + handler.getClass().getName());
      return handler;
    }
  }
  
  private Handler loadFileHandler(final JDKLoggerPluginConfiguration confirguration) throws Exception {
    return new FileHandler(confirguration.fileHandlerPattern(), confirguration.fileHandlerLimit(), confirguration.fileHandlerCount(), confirguration.fileHandlerAppend());
  }

  private Handler loadMemoryHandler(final JDKLoggerPluginConfiguration confirguration) throws Exception {
    final String message = "Must correctly configure target, size, and pushLevel for logging MemoryHandler.";

    if (confirguration.memoryHandlerTarget() == null ||
            confirguration.memoryHandlerSize() == -1 ||
            confirguration.memoryHandlerPushLevel() == null) {
      throw new IllegalArgumentException(message);
    }

    try {
      Class<?> targetClass = Class.forName(confirguration.memoryHandlerTarget());
      return new MemoryHandler((Handler) targetClass.newInstance(), confirguration.memoryHandlerSize(), Level.parse(confirguration.memoryHandlerPushLevel()));
    } catch (Exception e) {
      throw new IllegalArgumentException(message, e);
    }
  }

  private Handler loadNamedHandler(final JDKLoggerPluginConfiguration confirguration) throws Exception {
    return confirguration.handlerClass().newInstance();
  }
}
