// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.logging.noop;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.actors.LoggerProvider;

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
