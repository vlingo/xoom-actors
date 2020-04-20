// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.slf4j;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Address;
import io.vlingo.actors.Logger;
import io.vlingo.actors.logging.LogEvent;
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

  private static void withMcd(final LogEvent logEvent, Runnable runWithMdc) {
    try {
      //Populate the MDC http://www.slf4j.org/manual.html#mdc
      final Address sourceActorAddress = logEvent.getSourceActorAddress();
      if (sourceActorAddress != null) {
        MDC.put(MDC_KEY_SOURCE_ADDRESS, sourceActorAddress.name());
      }
      if (logEvent.getSourceThread() != null) {
        MDC.put(MDC_KEY_SOURCE_THREAD, logEvent.getSourceThread());
      }
      if (logEvent.getEventTimestamp() != null) {
        MDC.put(MDC_KEY_EVENT_TIMESTAMP, logEvent.getEventTimestamp().toString());
      }

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
