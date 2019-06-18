// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.plugin.logging.noop.NoOpLogger;
import io.vlingo.actors.plugin.logging.slf4j.Slf4jLoggerPlugin;

public interface Logger {
  static Logger noOpLogger() {
    return new NoOpLogger();
  }

  static Logger basicLogger() {
    return Slf4jLoggerPlugin.basicInstance();
  }
  
  String name();
  void close();
  boolean isEnabled();

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
