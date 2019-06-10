package io.vlingo.actors.plugin.logging.jdk;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Logger;

public class JDKLoggerActor extends Actor implements Logger {
  private final JDKLogger logger;

  public JDKLoggerActor(final JDKLogger logger) {
    if (logger == null) {
      throw new NullPointerException("JDKLogger can not be null");
    }

    this.logger = logger;
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
  public void log(final String message) {
    logger.log(message);
  }

  @Override
  public void log(final String message, final Throwable throwable) {
    logger.log(message, throwable);
  }

  @Override
  public void trace(String message) {
    logger.trace(message);
  }

  @Override
  public void trace(String message, Object... args) {
    logger.trace(message, args);
  }

  @Override
  public void trace(String message, Throwable throwable) {
    logger.trace(message, throwable);
  }

  @Override
  public void debug(String message) {
    logger.debug(message);
  }

  @Override
  public void debug(String message, Object... args) {
    logger.debug(message, args);
  }

  @Override
  public void debug(String message, Throwable throwable) {
    logger.debug(message, throwable);
  }

  @Override
  public void info(String message) {
    logger.info(message);
  }

  @Override
  public void info(String message, Object... args) {
    logger.info(message, args);
  }

  @Override
  public void info(String message, Throwable throwable) {
    logger.info(message, throwable);
  }

  @Override
  public void warn(String message) {
    logger.warn(message);
  }

  @Override
  public void warn(String message, Object... args) {
    logger.warn(message, args);
  }

  @Override
  public void warn(String message, Throwable throwable) {
    logger.warn(message, throwable);
  }

  @Override
  public void error(String message) {
    logger.error(message);
  }

  @Override
  public void error(String message, Object... args) {
    logger.error(message, args);
  }

  @Override
  public void error(String message, Throwable throwable) {
    logger.error(message, throwable);
  }

  @Override
  public String name() {
    return logger.name();
  }

  @Override
  public void stop() {
    close();
    super.stop();
  }
}
