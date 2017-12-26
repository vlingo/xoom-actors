// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.logging.jdk;

import io.vlingo.actors.Logger;

public class JDKLogger implements Logger {
  private final java.util.logging.Logger logger;
  private final String name;

  public static Logger testInstance() {
    return new JDKLogger("test", "vlingo-test");
  }
  
  protected JDKLogger(final String name, final String loggerName) {
    this.name = name;
    this.logger = java.util.logging.Logger.getLogger(loggerName);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void log(final String message) {
    logger.log(java.util.logging.Level.ALL, message);
  }

  @Override
  public String name() {
    return name;
  }
}
