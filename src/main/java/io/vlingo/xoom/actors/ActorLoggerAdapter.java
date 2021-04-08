// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import io.vlingo.xoom.actors.logging.LogEvent;

public class ActorLoggerAdapter implements Logger {
  private final Logger logger;
  private final Address sourceActorAddress;
  private final Class<?> sourceActorType;

  public static ActorLoggerAdapter from(final Class<?> sourceActorType, final Logger logger) {
    return new ActorLoggerAdapter(sourceActorType, null, logger);
  }

  static ActorLoggerAdapter from(final Class<? extends Actor> sourceActorType, final Address sourceActorAddress, final Logger logger) {
    return new ActorLoggerAdapter(sourceActorType, sourceActorAddress, logger);
  }

  private ActorLoggerAdapter(final Class<?> sourceActorType, final Address sourceActorAddress, final Logger logger) {
    this.logger = logger;
    this.sourceActorAddress = sourceActorAddress;
    this.sourceActorType = sourceActorType;
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
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).build();
    trace(logEvent);
  }

  @Override
  public void trace(final String message, final Object... args) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).withArgs(args).build();
    trace(logEvent);
  }

  @Override
  public void trace(final String message, final Throwable throwable) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).withThrowable(throwable).build();
    trace(logEvent);
  }

  @Override
  public void debug(final String message) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).build();
    debug(logEvent);
  }

  @Override
  public void debug(final String message, final Object... args) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).withArgs(args).build();
    debug(logEvent);
  }

  @Override
  public void debug(final String message, final Throwable throwable) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).withThrowable(throwable).build();
    debug(logEvent);
  }

  @Override
  public void info(final String message) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).build();
    info(logEvent);
  }

  @Override
  public void info(final String message, final Object... args) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).withArgs(args).build();
    info(logEvent);
  }

  @Override
  public void info(final String message, final Throwable throwable) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).withThrowable(throwable).build();
    info(logEvent);
  }

  @Override
  public void warn(final String message) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).build();
    warn(logEvent);
  }

  @Override
  public void warn(final String message, final Object... args) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).withArgs(args).build();
    warn(logEvent);
  }

  @Override
  public void warn(final String message, final Throwable throwable) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).withThrowable(throwable).build();
    warn(logEvent);
  }

  @Override
  public void error(final String message) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).build();
    error(logEvent);
  }

  @Override
  public void error(final String message, final Object... args) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).withArgs(args).build();
    error(logEvent);
  }

  @Override
  public void error(final String message, final Throwable throwable) {
    final LogEvent logEvent = new LogEvent.Builder(sourceActorType, message).withSourceActorAddress(this.sourceActorAddress).withThrowable(throwable).build();
    error(logEvent);
  }

  @Override
  public void trace(final LogEvent logEvent) {
    this.logger.trace(logEvent);
  }

  @Override
  public void debug(final LogEvent logEvent) {
    this.logger.debug(logEvent);
  }

  @Override
  public void info(final LogEvent logEvent) {
    this.logger.info(logEvent);
  }

  @Override
  public void warn(final LogEvent logEvent) {
    this.logger.warn(logEvent);
  }

  @Override
  public void error(final LogEvent logEvent) {
    this.logger.error(logEvent);
  }
}
