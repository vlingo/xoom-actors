// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.noop;

import io.vlingo.actors.LoggerProvider;
import io.vlingo.actors.plugin.logging.Logger;

public class NoOpLoggerProvider implements LoggerProvider {
  private final Logger logger;
  
  public NoOpLoggerProvider() {
    this.logger = new NoOpLogger();
  }

  @Override
  public void close() { }

  @Override
  public Logger logger() {
    return logger;
  }
}
