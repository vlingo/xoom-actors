package io.vlingo.actors;

import io.vlingo.actors.plugin.logging.Logger;
import io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin;
import io.vlingo.actors.plugin.logging.noop.NoOpLoggerProvider;
import io.vlingo.actors.plugin.logging.sysout.SystemOutLoggerPlugin;

public interface LoggerProvider {
  public static LoggerProvider noOpLoggerProvider() {
    return new NoOpLoggerProvider();
  }

  public static LoggerProvider standardLoggerProvider() {
    return new JDKLoggerPlugin();
  }

  public static LoggerProvider systemOutLoggerProvider() {
    return new SystemOutLoggerPlugin();
  }

  void close();
  Logger logger();
}
