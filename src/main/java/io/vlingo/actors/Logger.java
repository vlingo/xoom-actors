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

  void close();
  boolean isEnabled();
  void log(final String message);
  void log(final String message, final Throwable throwable);
  String name();
}
