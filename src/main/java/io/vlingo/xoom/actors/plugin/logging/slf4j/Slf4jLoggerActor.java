// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.logging.slf4j;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.actors.logging.LogEvent;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class Slf4jLoggerActor extends Actor implements Logger {
  private static final String LOGGER_NAME = "Slf4jLogger";

  private static final String MDC_KEY_SOURCE_THREAD = "sourceThread";
  private static final String MDC_KEY_SOURCE_ADDRESS = "sourceAddress";
  private static final String MDC_KEY_EVENT_TIMESTAMP = "eventTimestamp";

  private final Map<Class<?>, org.slf4j.Logger> loggerMap = new HashMap<>();

  public Slf4jLoggerActor() {
  }

  @Override
  public void stop() {
    close();
    super.stop();
  }

  @Override
  public String name() {
    return LOGGER_NAME;
  }

  @Override
  public void close() {
    this.loggerMap.clear();
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void trace(final LogEvent logEvent) {
    withMcd(logEvent, () -> {
      getSlf4jLogger(logEvent).trace(logEvent.getMessage(), logEvent.getArgs(), logEvent.getThrowable());
    });
  }

  @Override
  public void debug(final LogEvent logEvent) {
    withMcd(logEvent, () -> {
      getSlf4jLogger(logEvent).debug(logEvent.getMessage(), logEvent.getArgs(), logEvent.getThrowable());
    });
  }

  @Override
  public void info(final LogEvent logEvent) {
    withMcd(logEvent, () -> {
      getSlf4jLogger(logEvent).info(logEvent.getMessage(), logEvent.getArgs(), logEvent.getThrowable());
    });
  }

  @Override
  public void warn(final LogEvent logEvent) {
    withMcd(logEvent, () -> {
      getSlf4jLogger(logEvent).warn(logEvent.getMessage(), logEvent.getArgs(), logEvent.getThrowable());
    });
  }

  @Override
  public void error(final LogEvent logEvent) {
    withMcd(logEvent, () -> {
      getSlf4jLogger(logEvent).error(logEvent.getMessage(), logEvent.getArgs(), logEvent.getThrowable());
    });
  }

  private org.slf4j.Logger getSlf4jLogger(final LogEvent logEvent) {
    return loggerMap.computeIfAbsent(logEvent.getSource(), LoggerFactory::getLogger);
  }

  /**
   * Run the code with configure SLF4J <a href="http://www.slf4j.org/manual.html#mdc">MDC</a>.
   * <p>
   * In order to log the MDC field, the underlying SLF4J library has to be configured.
   * Example of a Logback configuration:
   * <code>
   * <pattern>%d{HH:mm:ss.SSS}[%thread][%X{eventTimestamp}][%X{sourceThread}][%X{sourceAddress}] %-5level %logger{36} - %msg%n</pattern>
   * </code>
   *
   * @param logEvent   the log event
   * @param runWithMdc the code that will run with configured MDC
   */
  private static void withMcd(final LogEvent logEvent, Runnable runWithMdc) {
    try {
      //Populate the MDC
      logEvent.getSourceActorAddress().ifPresent(address -> MDC.put(MDC_KEY_SOURCE_ADDRESS, address.name()));
      logEvent.getSourceThread().ifPresent(sourceThread -> MDC.put(MDC_KEY_SOURCE_THREAD, sourceThread));
      logEvent.getEventTimestamp().ifPresent(instant -> MDC.put(MDC_KEY_EVENT_TIMESTAMP, instant.toString()));

      //Run
      runWithMdc.run();

    } finally {
      //Clear the known keys from MDC
      MDC.remove(MDC_KEY_SOURCE_THREAD);
      MDC.remove(MDC_KEY_EVENT_TIMESTAMP);
      MDC.remove(MDC_KEY_SOURCE_ADDRESS);
    }
  }

}
