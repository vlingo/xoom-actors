// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.logging.slf4j;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.actors.logging.LogEvent;
import org.slf4j.LoggerFactory;

public class Slf4jLogger implements Logger {

  private final org.slf4j.Logger logger = LoggerFactory.getLogger(Logger.class);
  private final String name;

  Slf4jLogger(final String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void close() {

  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void trace(String message) {
    this.logger.trace(message);
  }

  @Override
  public void trace(String message, Object... args) {
    this.logger.trace(message, args);
  }

  @Override
  public void trace(String message, Throwable throwable) {
    this.logger.trace(message, throwable);
  }

  @Override
  public void debug(String message) {
    this.logger.debug(message);
  }

  @Override
  public void debug(String message, Object... args) {
    this.logger.debug(message, args);
  }

  @Override
  public void debug(String message, Throwable throwable) {
    this.logger.debug(message, throwable);
  }

  @Override
  public void info(String message) {
    this.logger.info(message);
  }

  @Override
  public void info(String message, Object... args) {
    this.logger.info(message, args);
  }

  @Override
  public void info(String message, Throwable throwable) {
    this.logger.info(message, throwable);
  }

  @Override
  public void warn(String message) {
    this.logger.warn(message);
  }

  @Override
  public void warn(String message, Object... args) {
    this.logger.warn(message, args);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    this.logger.warn(message, throwable);
  }

  @Override
  public void error(String message) {
    this.logger.error(message);
  }

  @Override
  public void error(String message, Object... args) {
    this.logger.error(message, args);
  }

  @Override
  public void error(String message, Throwable throwable) {
    this.logger.error(message, throwable);
  }

  @Override
  public void trace(final LogEvent logEvent) {
    this.logger.trace(logEvent.getMessage(), logEvent.getArgs(), logEvent.getThrowable());
  }

  @Override
  public void debug(final LogEvent logEvent) {
    this.logger.debug(logEvent.getMessage(), logEvent.getArgs(), logEvent.getThrowable());
  }

  @Override
  public void info(final LogEvent logEvent) {
    this.logger.info(logEvent.getMessage(), logEvent.getArgs(), logEvent.getThrowable());
  }

  @Override
  public void warn(final LogEvent logEvent) {
    this.logger.warn(logEvent.getMessage(), logEvent.getArgs(), logEvent.getThrowable());
  }

  @Override
  public void error(final LogEvent logEvent) {
    this.logger.error(logEvent.getMessage(), logEvent.getArgs(), logEvent.getThrowable());
  }
}
