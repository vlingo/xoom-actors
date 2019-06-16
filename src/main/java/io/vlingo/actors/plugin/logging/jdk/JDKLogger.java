// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.jdk;

import io.vlingo.actors.Logger;
import io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin.JDKLoggerPluginConfiguration;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.MemoryHandler;

public class JDKLogger implements Logger {
  private final Handler handler;
  private final Level level;
  private final java.util.logging.Logger logger;
  private final String name;

  //TODO remove when dependencies are updated.
  public static Logger testInstance() {
    return JDKLoggerPlugin.testInstance();
  }

  protected JDKLogger(final String name, final JDKLoggerPluginConfiguration configuration) {
    this.name = name;
    this.logger = java.util.logging.Logger.getLogger(name);
    this.level = java.util.logging.Level.parse(configuration.handlerLevel());
    this.logger.setLevel(this.level);
    this.handler = determineHandler(configuration);
    this.logger.addHandler(handler);
    this.handler.setLevel(this.level);
    //Disable default console handler.
    this.logger.setUseParentHandlers(false);
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
      debug(message);
    }
  }

  @Override
  public void log(final String message, final Throwable throwable) {
    if (isEnabled()) {
      debug(message, throwable);
    }
  }

  @Override
  public void trace(String message) {
    log(Level.FINEST, message);
  }

  @Override
  public void trace(String message, Object... args) {
    log(Level.FINEST, message, args);
  }

  @Override
  public void trace(String message, Throwable throwable) {
    log(Level.FINEST, message, throwable);
  }

  @Override
  public void debug(String message) {
    log(Level.FINE, message);
  }

  @Override
  public void debug(String message, Object... args) {
    log(Level.FINE, message, args);
  }

  @Override
  public void debug(String message, Throwable throwable) {
    log(Level.FINE, message, throwable);
  }

  @Override
  public void info(String message) {
    log(Level.INFO, message);
  }

  @Override
  public void info(String message, Object... args) {
    log(Level.INFO, message, args);
  }

  @Override
  public void info(String message, Throwable throwable) {
    log(Level.INFO, message, throwable);
  }

  @Override
  public void warn(String message) {
    log(Level.WARNING, message);
  }

  @Override
  public void warn(String message, Object... args) {
    log(Level.WARNING, message, args);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    log(Level.WARNING, message, throwable);
  }

  @Override
  public void error(String message) {
    log(Level.SEVERE, message);
  }

  @Override
  public void error(String message, Object... args) {
    log(Level.SEVERE, message, args);
  }

  @Override
  public void error(String message, Throwable throwable) {
    log(Level.SEVERE, message, throwable);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " TYPE: " + handler;
  }

  private void log(Level level, String message) {
    if (isEnabled()) {
      this.logger.log(level, message);
    }
  }

  private void log(Level level, String message, Object... args) {
    if (isEnabled()) {
      this.logger.log(level, message, args);
    }
  }

  private void log(Level level, String message, Throwable throwable) {
    if (isEnabled()) {
      this.logger.log(level, message, throwable);
    }
  }

  private Handler determineHandler(final JDKLoggerPluginConfiguration configuration) {
    final String classname = configuration.handlerClass().getName();
    try {
      if (classname.equals("java.util.logging.FileHandler")) {
        return loadFileHandler(configuration);
      } else if (classname.equals("java.util.logging.MemoryHandler")) {
        return loadMemoryHandler(configuration);
      } else {
        return loadNamedHandler(configuration);
      }

    } catch (Exception e) {
      final Handler handler = new DefaultHandler();
      this.logger.addHandler(handler);
      this.logger.log(this.level,
              "vlingo/actors: Could not load the logger " + configuration.name() + " because: " + e.getMessage(), e);
      this.logger.log(this.level, "vlingo/actors: Instead we defaulted to: " + handler.getClass().getName());
      return handler;
    }
  }

  private Handler loadFileHandler(final JDKLoggerPluginConfiguration confirguration) throws Exception {
    return new FileHandler(confirguration.fileHandlerPattern(), confirguration.fileHandlerLimit(),
            confirguration.fileHandlerCount(), confirguration.fileHandlerAppend());
  }

  private Handler loadMemoryHandler(final JDKLoggerPluginConfiguration confirguration) throws Exception {
    final String message = "Must correctly configure target, size, and pushLevel for logging MemoryHandler.";

    if (confirguration.memoryHandlerTarget() == null || confirguration.memoryHandlerSize() == -1
            || confirguration.memoryHandlerPushLevel() == null) {
      throw new IllegalArgumentException(message);
    }

    try {
      Class<?> targetClass = Class.forName(confirguration.memoryHandlerTarget());
      return new MemoryHandler((Handler) targetClass.newInstance(), confirguration.memoryHandlerSize(),
              Level.parse(confirguration.memoryHandlerPushLevel()));
    } catch (Exception e) {
      throw new IllegalArgumentException(message, e);
    }
  }

  private Handler loadNamedHandler(final JDKLoggerPluginConfiguration confirguration) throws Exception {
    return confirguration.handlerClass().newInstance();
  }
}
