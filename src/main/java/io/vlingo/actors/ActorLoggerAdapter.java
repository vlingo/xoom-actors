// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

public class ActorLoggerAdapter implements Logger {
  final Logger logger;
  final String prefix;

  ActorLoggerAdapter(final Logger logger, final String prefix) {
    this.logger = logger;
    this.prefix = prefix;
  }

  @Override
  public String name() {
    return logger.name();
  }

  @Override
  public void close() {
    logger.close();
  }

  @Override
  public boolean isEnabled() {
    return logger.isEnabled();
  }

  @Override
  public void trace(final String message) {
    logger.trace(prefix(message));
  }

  @Override
  public void trace(final String message, final Object... args) {
    logger.trace(prefix(message), args);
  }

  @Override
  public void trace(final String message, final Throwable throwable) {
    logger.trace(prefix(message), throwable);
  }

  @Override
  public void debug(final String message) {
    logger.debug(prefix(message));
  }

  @Override
  public void debug(final String message, final Object... args) {
    logger.debug(prefix(message), args);
  }

  @Override
  public void debug(final String message, final Throwable throwable) {
    logger.debug(prefix(message), throwable);
  }

  @Override
  public void info(final String message) {
    logger.info(prefix(message));
  }

  @Override
  public void info(final String message, final Object... args) {
    logger.info(prefix(message), args);
  }

  @Override
  public void info(final String message, final Throwable throwable) {
    logger.info(prefix(message), throwable);
  }

  @Override
  public void warn(final String message) {
    logger.warn(prefix(message));
  }

  @Override
  public void warn(final String message, final Object... args) {
    logger.warn(prefix(message), args);
  }

  @Override
  public void warn(final String message, final Throwable throwable) {
    logger.warn(prefix(message), throwable);
  }

  @Override
  public void error(final String message) {
    logger.error(prefix(message));
  }

  @Override
  public void error(final String message, final Object... args) {
    logger.error(prefix(message), args);
  }

  @Override
  public void error(final String message, final Throwable throwable) {
    logger.error(prefix(message), throwable);
  }

  private String prefix(final String message) {
    return prefix + " - " + message;
  }
}
