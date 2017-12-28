// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin;
import io.vlingo.actors.plugin.logging.noop.NoOpLoggerProvider;

public interface LoggerProvider {
  public static LoggerProvider noOpLoggerProvider() {
    return new NoOpLoggerProvider();
  }

  public static LoggerProvider standardLoggerProvider(final World world, final String name) {
    return JDKLoggerPlugin.registerStandardLogger(name, world);
  }

  void close();
  Logger logger();
}
