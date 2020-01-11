// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.slf4j;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorInstantiator;
import io.vlingo.actors.Logger;

public class Slf4jLoggerActor extends Actor implements Logger {
  private final Logger logger;

  public Slf4jLoggerActor(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void stop() {
    close();
    super.stop();
  }

  @Override
  public String name() {
    return this.logger.name();
  }

  @Override
  public void close() {
    this.logger.close();
  }

  @Override
  public boolean isEnabled() {
    return this.logger.isEnabled();
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

  public static class Slf4jLoggerInstantiator implements ActorInstantiator<Slf4jLoggerActor> {
    private final Logger logger;

    public Slf4jLoggerInstantiator(final Logger logger) {
      this.logger = logger;
    }

    @Override
    public Slf4jLoggerActor instantiate() {
      return new Slf4jLoggerActor(logger);
    }

    @Override
    public Class<Slf4jLoggerActor> type() {
      return Slf4jLoggerActor.class;
    }
  }
}
