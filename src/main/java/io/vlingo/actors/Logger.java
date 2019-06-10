// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.plugin.logging.jdk.JDKLogger;
import io.vlingo.actors.plugin.logging.noop.NoOpLogger;

public interface Logger {
  public static Logger noOpLogger() {
    return new NoOpLogger();
  }

  public static Logger basicLogger() {
    return JDKLogger.basicInstance();
  }

  public static Logger testLogger() {
    return JDKLogger.testInstance();
  }

  String name();
  void close();
  boolean isEnabled();

  void log(final String message);
  void log(final String message, final Throwable throwable);

  void trace(String message);
  void trace(String message, Object... args);
  void trace(String message, final Throwable throwable);

  void debug(String message);
  void debug(String message, Object... args);
  void debug(String message, final Throwable throwable);

  void info(String message);
  void info(String message, Object... args);
  void info(String message, final Throwable throwable);
  
  void warn(String message);
  void warn(String message, Object... args);
  void warn(String message, final Throwable throwable);

  void error(String message);
  void error(String message, Object... args);
  void error(String message, final Throwable throwable);

}
