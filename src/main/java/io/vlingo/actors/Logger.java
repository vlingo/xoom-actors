// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
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

  void trace(final String message);
  void trace(final String message, final Object... args);
  void trace(final String message, final Throwable throwable);

  void debug(final String message);
  void debug(final String message, final Object... args);
  void debug(final String message, final Throwable throwable);

  void info(final String message);
  void info(final String message, final Object... args);
  void info(final String message, final Throwable throwable);

  void warn(final String message);
  void warn(final String message, final Object... args);
  void warn(final String message, final Throwable throwable);

  void error(final String message);
  void error(final String message, final Object... args);
  void error(final String message, final Throwable throwable);

}
