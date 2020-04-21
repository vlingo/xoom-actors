// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.logging.LogEvent;
import io.vlingo.actors.plugin.logging.noop.NoOpLogger;
import io.vlingo.actors.plugin.logging.slf4j.Slf4jLoggerPlugin;

public interface Logger {
  static Logger noOpLogger() {
    return new NoOpLogger();
  }

  static Logger basicLogger() {
    return Slf4jLoggerPlugin.basicInstance();
  }

  String name();

  void close();

  boolean isEnabled();

  default void trace(final String message) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).build();
    trace(logEvent);
  }

  default void trace(final String message, final Object... args) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).withArgs(args).build();
    trace(logEvent);
  }

  default void trace(final String message, final Throwable throwable) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).withThrowable(throwable).build();
    trace(logEvent);
  }

  default void debug(final String message) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).build();
    debug(logEvent);
  }

  default void debug(final String message, final Object... args) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).withArgs(args).build();
    debug(logEvent);
  }

  default void debug(final String message, final Throwable throwable) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).withThrowable(throwable).build();
    debug(logEvent);
  }

  default void info(final String message) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).build();
    info(logEvent);
  }

  default void info(final String message, final Object... args) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).withArgs(args).build();
    info(logEvent);
  }

  default void info(final String message, final Throwable throwable) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).withThrowable(throwable).build();
    info(logEvent);
  }

  default void warn(final String message) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).build();
    warn(logEvent);
  }

  default void warn(final String message, final Object... args) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).withArgs(args).build();
    warn(logEvent);
  }

  default void warn(final String message, final Throwable throwable) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).withThrowable(throwable).build();
    warn(logEvent);
  }

  default void error(final String message) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).build();
    error(logEvent);
  }

  default void error(final String message, final Object... args) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).withArgs(args).build();
    error(logEvent);
  }

  default void error(final String message, final Throwable throwable) {
    final LogEvent logEvent = new LogEvent.Builder(Logger.class, message).withThrowable(throwable).build();
    error(logEvent);
  }

  void trace(final LogEvent logEvent);

  void debug(final LogEvent logEvent);

  void info(final LogEvent logEvent);

  void warn(final LogEvent logEvent);

  void error(final LogEvent logEvent);
}
