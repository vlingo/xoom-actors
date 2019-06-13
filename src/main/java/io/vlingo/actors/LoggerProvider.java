// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.plugin.logging.noop.NoOpLoggerProvider;
import io.vlingo.actors.plugin.logging.slf4j.Slf4jLoggerPlugin;

public interface LoggerProvider {
  static LoggerProvider noOpLoggerProvider() {
    return new NoOpLoggerProvider();
  }

  static LoggerProvider standardLoggerProvider(final World world, final String name) {
    return Slf4jLoggerPlugin.registerStandardLogger(name, world);
  }

  void close();
  
  Logger logger();
}
