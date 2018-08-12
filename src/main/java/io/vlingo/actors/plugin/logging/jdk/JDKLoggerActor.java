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
  public String name() {
    return logger.name();
  }

  @Override
  public void stop() {
    close();
    super.stop();
  }
}
